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
import com.ngdesk.websocket.channels.chat.dao.DataProxy;
import com.ngdesk.websocket.companies.dao.Company;
import com.ngdesk.websocket.modules.dao.Module;

@Component
public class CloseSessionJob {

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

	@Scheduled(fixedRate = 60000)
	public void run() {

		try {
			RMap<Long, Map<String, Object>> usersMap = redisson.getMap("disconnectedCustomers");
			String epochDate = "01/01/1970";
			Date date = new SimpleDateFormat("dd/MM/yyyy").parse(epochDate);
			Timestamp epoch = new Timestamp(date.getTime());
			Timestamp today = new Timestamp(new Date().getTime());
			long currentTimeDiff = today.getTime() - epoch.getTime();

			for (Long timeDiff : usersMap.keySet()) {
				if (currentTimeDiff >= timeDiff) {
					Map<String, Object> userMap = usersMap.get(timeDiff);
					String sessionUUID = userMap.get("SESSION_UUID").toString();
					String subdomain = userMap.get("SUBDOMAIN").toString();
					ConcurrentHashMap<String, UserSessions> userSessionsMap = sessionService.sessions.get(subdomain);
					boolean isCustomerOffline = false;
					if (userSessionsMap != null && userSessionsMap.containsKey(sessionUUID)) {
						if (userSessionsMap.get(sessionUUID).getSessions() != null) {
							if (userSessionsMap.get(sessionUUID).getSessions().size() == 0) {
								userSessionsMap.remove(sessionUUID);
								isCustomerOffline = true;
								usersMap.remove(timeDiff);
							}
							if (userSessionsMap.size() == 0) {
								sessionService.sessions.remove(sessionUUID);
								isCustomerOffline = true;
								usersMap.remove(timeDiff);
							}

						}
					}

					if (isCustomerOffline) {
						updateChatEntry(sessionUUID, subdomain);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void updateChatEntry(String sessionUUID, String subdomain) {

		Optional<Company> optionalCompany = companiesRepository.findCompanyBySubdomain(subdomain);
		if (optionalCompany.isPresent()) {
			Company company = optionalCompany.get();
			String companyId = company.getId();
			Optional<Module> optionalChatModule = modulesRepository.findModuleByName("Chats", "modules_" + companyId);
			if (optionalChatModule.isPresent()) {
				Optional<Map<String, Object>> optionalChatEntry = moduleEntryRepository.findBySessionUuid(sessionUUID,
						"Chats_" + companyId);
				if (optionalChatEntry.isPresent() && !optionalChatEntry.get().get("STATUS").equals("Offline")) {
					Map<String, Object> chatEntry = optionalChatEntry.get();
					String customerId = chatEntry.get("REQUESTOR").toString();
					Optional<Map<String, Object>> optionalCustomerContactEntry = moduleEntryRepository
							.findById(customerId, "Contacts_" + companyId);
					if (optionalCustomerContactEntry.isPresent()) {
						String firstName = optionalCustomerContactEntry.get().get("FIRST_NAME").toString();
						String lastName = optionalCustomerContactEntry.get().get("LAST_NAME").toString();
						Map<String, Object> customerContactEntry = optionalCustomerContactEntry.get();
						Optional<Map<String, Object>> optionalCustomerUserEntry = moduleEntryRepository
								.findById(customerContactEntry.get("USER").toString(), "Users_" + companyId);
						if (optionalCustomerUserEntry.isPresent()) {
							String roleId = optionalCustomerUserEntry.get().get("ROLE").toString();
							String userUuid = optionalCustomerUserEntry.get().get("USER_UUID").toString();
							HashMap<String, Object> entry = new HashMap<String, Object>();
							entry.put("STATUS", "Offline");
							entry.put("DATA_ID", optionalChatEntry.get().get("_id").toString());
							Sender sender = new Sender(firstName, lastName, userUuid, roleId);
							List<DiscussionMessage> messages = new ArrayList<DiscussionMessage>();
							String message = global.getFile("metadata_customer_disconnected.html");
							message = message.replace("NAME_REPLACE", firstName + " " + lastName);
							DiscussionMessage discussionMessage = new DiscussionMessage(message, new Date(),
									UUID.randomUUID().toString(), "META_DATA", new ArrayList<MessageAttachment>(),
									sender, null, null, null);
							messages.add(discussionMessage);
							entry.put("CHAT", messages);
							dataProxy.putModuleEntry(entry, optionalChatModule.get().getModuleId(), false, companyId,
									optionalCustomerUserEntry.get().get("USER_UUID").toString());
						}
					}
				}
			}
		}

	}

}