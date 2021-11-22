package com.ngdesk.websocket.jobs;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.ngdesk.commons.Global;
import com.ngdesk.data.dao.DiscussionMessage;
import com.ngdesk.data.dao.MessageAttachment;
import com.ngdesk.data.dao.Sender;
import com.ngdesk.repositories.CompaniesRepository;
import com.ngdesk.repositories.ModuleEntryRepository;
import com.ngdesk.repositories.ModulesRepository;
import com.ngdesk.websocket.SessionService;
import com.ngdesk.websocket.UserSessions;
import com.ngdesk.websocket.channels.chat.dao.ChatService;
import com.ngdesk.websocket.channels.chat.dao.ChatTicketStatusMessage;
import com.ngdesk.websocket.channels.chat.dao.DataProxy;
import com.ngdesk.websocket.companies.dao.Company;
import com.ngdesk.websocket.modules.dao.Module;

@Component
public class AgentSessionExpiry {

	@Autowired
	RedissonClient redisson;

	@Autowired
	SessionService sessionService;

	@Autowired
	CompaniesRepository companiesRepository;

	@Autowired
	ModulesRepository modulesRepository;

	@Autowired
	ModuleEntryRepository moduleEntryRepository;

	@Autowired
	DataProxy dataProxy;

	@Autowired
	Global global;

	@Autowired
	ChatService chatService;

	@Scheduled(fixedRate = 60000)
	public void run() {
		try {
			RMap<Long, Map<String, Object>> usersMap = redisson.getMap("disconnectedUsers");
			String epochDate = "01/01/1970";
			Date date = new SimpleDateFormat("dd/MM/yyyy").parse(epochDate);
			Timestamp epoch = new Timestamp(date.getTime());

			Timestamp today = new Timestamp(new Date().getTime());
			long currentTimeDiff = today.getTime() - epoch.getTime();
			for (Long timeDiff : usersMap.keySet()) {

				if (currentTimeDiff >= timeDiff) {
					Map<String, Object> userMap = usersMap.get(timeDiff);
					String userId = userMap.get("USER_ID").toString();
					String subdomain = userMap.get("SUBDOMAIN").toString();
					ConcurrentHashMap<String, UserSessions> userSessionsMap = sessionService.sessions.get(subdomain);
					boolean isUserOffline = false;
					if (userSessionsMap != null && userSessionsMap.containsKey(userId)) {
						if (userSessionsMap.get(userId).getSessions() != null) {
							if (userSessionsMap.get(userId).getSessions().size() == 0) {
								usersMap.remove(timeDiff);
								sessionService.sessions.get(subdomain).remove(userId);
								isUserOffline = true;
							} else if (userSessionsMap.get(userId).getSessions().size() > 1) {
								usersMap.remove(timeDiff);

							}

						}

					}
					if (isUserOffline) {
						updateChatEntry(userId, subdomain);
					}

				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void updateChatEntry(String userId, String subdomain) {

		Optional<Company> optionalCompany = companiesRepository.findCompanyBySubdomain(subdomain);
		if (optionalCompany.isPresent()) {
			Company company = optionalCompany.get();
			String companyId = company.getId();
			Optional<Module> optionalChatModule = modulesRepository.findModuleByName("Chats", "modules_" + companyId);
			if (optionalChatModule.isPresent()) {
				Optional<List<Map<String, Object>>> optionalChatEntries = moduleEntryRepository
						.findChatsByAgentId(userId, "Chats_" + companyId);
				Optional<Map<String, Object>> optionalUserAgentEntry = moduleEntryRepository.findById(userId,
						"Users_" + companyId);
				if (optionalUserAgentEntry.isPresent()) {
					Map<String, Object> userAgentEntry = optionalUserAgentEntry.get();
					String roleId = userAgentEntry.get("ROLE").toString();
					String userUuid = userAgentEntry.get("USER_UUID").toString();
					Optional<Map<String, Object>> optionalAgentContactEntry = moduleEntryRepository
							.findById(userAgentEntry.get("CONTACT").toString(), "Contacts_" + companyId);
					if (optionalAgentContactEntry.isPresent()) {
						String firstName = optionalAgentContactEntry.get().get("FIRST_NAME").toString();
						String lastName = optionalAgentContactEntry.get().get("LAST_NAME").toString();
						Sender sender = new Sender(firstName, lastName, userUuid, roleId);
						List<DiscussionMessage> messages = new ArrayList<DiscussionMessage>();
						String message = global.getFile("metadata_customer_disconnected.html");
						message = message.replace("NAME_REPLACE", firstName + " " + lastName);
						DiscussionMessage discussionMessage = new DiscussionMessage(message, new Date(),
								UUID.randomUUID().toString(), "META_DATA", new ArrayList<MessageAttachment>(), sender,
								null, null, null);
						messages.add(discussionMessage);
						for (Map<String, Object> chatEntry : optionalChatEntries.get()) {
							HashMap<String, Object> entry = new HashMap<String, Object>();
							entry.put("STATUS", "Offline");
							entry.put("DATA_ID", chatEntry.get("_id").toString());
							entry.put("CHAT", messages);
							dataProxy.putModuleEntry(entry, optionalChatModule.get().getModuleId(), false, companyId,
									optionalUserAgentEntry.get().get("USER_UUID").toString());

							ChatTicketStatusMessage chatTicketStatusMessage = new ChatTicketStatusMessage(companyId,
									chatEntry.get("SESSION_UUID").toString(), "CLOSE_CHAT", "CLOSED",
									"This chat ended by " + firstName + " " + lastName);
							chatService.addToChatTicketStatusQueue(chatTicketStatusMessage);
						}
					}
				}
			}
		}

	}

}
