package com.ngdesk.websocket.channels.chat.dao;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.expression.spel.ast.OpInc;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ngdesk.repositories.ChatChannelRepository;
import com.ngdesk.repositories.CompaniesRepository;
import com.ngdesk.repositories.ModuleEntryRepository;
import com.ngdesk.repositories.ModulesRepository;
import com.ngdesk.websocket.companies.dao.Company;
import com.ngdesk.websocket.modules.dao.Module;
import com.ngdesk.websocket.notification.dao.NotificationOfAgentDetails;

@Component
public class ChatService {

	@Autowired
	CompaniesRepository companiesRepository;

	@Autowired
	ModulesRepository modulesRepository;

	@Autowired
	ModuleEntryRepository moduleEntryRepository;

	@Autowired
	DataProxy dataProxy;

	@Autowired
	ChatChannelRepository chatChannelRepository;

	@Autowired
	ModuleEntryRepository entryRepository;

	@Autowired
	RedisTemplate<String, ChatChannelMessage> redisTemplate;

	@Autowired
	RedisTemplate<String, ChatNotification> redisTemplateForChatNotification;

	@Autowired
	RedisTemplate<String, NotificationOfAgentDetails> redisTemplateNotificationOfAgentDetails;

	public void publishPageLoad(ChatWidgetPayload pageLoad) {
		try {
			if (pageLoad.getSubdomain() != null) {
				Optional<Company> optionalCompany = companiesRepository.findCompanyBySubdomain(pageLoad.getSubdomain());
				if (optionalCompany.isPresent()) {
					Company company = optionalCompany.get();
					String companyId = company.getId();
					Optional<Module> optionalChatModule = modulesRepository.findModuleByName("Chat",
							"modules_" + companyId);
					if (optionalChatModule.isPresent()) {
						Optional<Map<String, Object>> optionalChatEntry = moduleEntryRepository
								.findBySessionUuid(pageLoad.getSessionUUID(), "Chat_" + companyId);
						Optional<Map<String, Object>> optionalUserEntry = moduleEntryRepository
								.findUserByEmailAddress("system@ngdesk.com", "Users_" + companyId);
						if (optionalUserEntry.isPresent()) {
							ObjectMapper mapper = new ObjectMapper();
							pageLoad.setCountry(Locale.getDefault().getDisplayCountry());
							Optional<ChatChannel> optionalChatChannel = chatChannelRepository
									.findChannelByName(pageLoad.getChannelName(), "channels_chat_" + companyId);
							if (optionalChatChannel.isPresent()) {
								ChatChannelMessage chatChannelMessage = new ChatChannelMessage(companyId,
										pageLoad.getSessionUUID(), optionalChatChannel.get(), "CHAT_CHANNEL");
								addToChatChannelQueue(chatChannelMessage);
								HashMap<String, Object> entry = (HashMap<String, Object>) mapper
										.readValue(mapper.writeValueAsString(pageLoad), Map.class);
								entry.put("CHANNEL", optionalChatChannel.get().getChannelId());
								entry.put("SOURCE_TYPE", "chat");
								Map<String, Object> user = optionalUserEntry.get();
								Map<String, Object> chatEntry = new HashMap<String, Object>();
								ChatNotification chatNotification = new ChatNotification();
								chatNotification.setCompanyId(companyId);
								chatNotification.setEntry(chatEntry);
								chatNotification.setSessionUUID(pageLoad.getSessionUUID());
								if (optionalChatEntry.isEmpty()) {
									entry.put("STATUS", "Browsing");
									chatEntry = dataProxy.postModuleEntry(entry, optionalChatModule.get().getModuleId(),
											false, companyId, user.get("USER_UUID").toString());
									chatNotification.setStatus("Browsing");
									chatNotification.setType("CHAT_ENTRY");

								} else {
									entry.put("DATA_ID", optionalChatEntry.get().get("_id").toString());
									chatEntry = dataProxy.putModuleEntry(entry, optionalChatModule.get().getModuleId(),
											false, companyId, user.get("USER_UUID").toString());
									chatNotification.setStatus("Chatting");
									chatNotification.setType("CHAT_ENTRY");
									publishAgentDetails(chatEntry, companyId);
								}
								addToChatNotificationQueue(chatNotification);
							}
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void addToChatChannelQueue(ChatChannelMessage chatChannelMessage) {
		redisTemplate.convertAndSend("chat_channel", chatChannelMessage);
	}

	public void addToChatNotificationQueue(ChatNotification message) {
		redisTemplateForChatNotification.convertAndSend("chat_notification", message);
	}

	public void publishAgentDetails(Map<String, Object> chatEntry, String companyId) {

		List<String> agents = (List<String>) chatEntry.get("AGENTS");
		Optional<Map<String, Object>> optionalUserEntry = entryRepository.findById(agents.get(0), "Users_" + companyId);

		if (optionalUserEntry.isPresent()) {
			Map<String, Object> agentUserEntry = optionalUserEntry.get();
			Optional<Map<String, Object>> optionalContactEntry = entryRepository
					.findById(agentUserEntry.get("CONTACT").toString(), "Contacts_" + companyId);
			String agentFirstName = null;
			String agentLastName = null;

			if (optionalContactEntry.isPresent()) {
				agentFirstName = optionalContactEntry.get().get("FIRST_NAME").toString();
				agentLastName = optionalContactEntry.get().get("LAST_NAME").toString();
				String agentRole = agentUserEntry.get("ROLE").toString();
				String customerId = chatEntry.get("REQUESTOR").toString();

				Optional<Map<String, Object>> optionalCustomerEntry = entryRepository.findById(customerId,
						"Users_" + companyId);
				if (optionalCustomerEntry.isPresent()) {
					Map<String, Object> customer = optionalCustomerEntry.get();
					String customerRole = customer.get("ROLE").toString();

					NotificationOfAgentDetails notificationOfAgentDetails = new NotificationOfAgentDetails(companyId,
							agentFirstName, agentLastName, agentUserEntry.get("_id").toString(),
							customer.get("_id").toString(), true, chatEntry.get("SESSION_UUID").toString(),
							"AGENTS_DATA", new Date(), agentRole, customerRole, customer.get("USER_UUID").toString(),
							chatEntry.get("_id").toString());
					redisTemplateNotificationOfAgentDetails.convertAndSend("agents_available",
							notificationOfAgentDetails);

				}
			}

		}

	}

}
