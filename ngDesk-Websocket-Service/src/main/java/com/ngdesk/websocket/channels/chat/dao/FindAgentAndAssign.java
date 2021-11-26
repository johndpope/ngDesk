package com.ngdesk.websocket.channels.chat.dao;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import com.ngdesk.commons.Global;
import com.ngdesk.data.dao.DiscussionMessage;
import com.ngdesk.data.dao.MessageAttachment;
import com.ngdesk.data.dao.Sender;
import com.ngdesk.repositories.ModuleEntryRepository;
import com.ngdesk.repositories.ModulesRepository;
import com.ngdesk.websocket.SessionService;
import com.ngdesk.websocket.UserSessions;
import com.ngdesk.websocket.companies.dao.ChatBusinessRules;
import com.ngdesk.websocket.companies.dao.Company;
import com.ngdesk.websocket.dao.WebSocketService;
import com.ngdesk.websocket.modules.dao.DataType;
import com.ngdesk.websocket.modules.dao.Module;
import com.ngdesk.websocket.modules.dao.ModuleField;
import com.ngdesk.websocket.notification.dao.AgentDetails;
import com.ngdesk.websocket.notification.dao.Notification;

@Component
public class FindAgentAndAssign {
	@Autowired
	ModulesRepository modulesRepository;

	@Autowired
	ModuleEntryRepository entryRepository;

	@Autowired
	SessionService sessionService;

	@Autowired
	RedisTemplate<String, Notification> redisTemplate;

	@Autowired
	Global global;

	@Autowired
	WebSocketService webSocketService;

	@Autowired
	DataProxy dataProxy;

	@Autowired
	AgentAvailabilityService agentAvailabilityService;

	@Autowired
	RedisTemplate<String, ChatNotification> redisTemplateForChatNotification;

	public void assignChatToAgent(Company company, ChatUser chatUser, Map<String, Object> customer) {

		String companyId = company.getId();
		String customerRole = customer.get("ROLE").toString();
		

		Optional<Module> optionalChatModule = modulesRepository.findModuleByName("Chats", "modules_" + company.getId());
		List<String> teamsWhoCanChat = company.getChatSettings().getTeamsWhoCanChat();
		ConcurrentHashMap<String, UserSessions> sessionMap = sessionService.sessions.get(company.getCompanySubdomain());

		String userId = null;
		Map<String, Object> agentUserEntry = null;
		List<String> agentsWhoCanChat = new ArrayList<String>();

		ChatBusinessRules businessRules = company.getChatSettings().getChatBusinessRules();
		Boolean hasRestriction = company.getChatSettings().getHasRestrictions();

		for (String key : sessionMap.keySet()) {
			userId = key;
			Optional<Map<String, Object>> optionalUserEntry = entryRepository.findById(userId, "Users_" + companyId);
			if (optionalUserEntry.isPresent()) {
				Integer chatEntries = entryRepository.findByAgentAndCollectionName(userId.toString(),
						"Chats_" + company.getId());
				UserSessions userSessions = sessionMap.get(key);
				String chatStatus = userSessions.getChatStatus();
				if (hasRestriction == null || !hasRestriction) {
					if (chatEntries < company.getChatSettings().getMaxChatPerAgent() && chatStatus != null
							&& chatStatus.equalsIgnoreCase("available")) {
						agentsWhoCanChat.add(key);
					}
				} else if (hasRestriction) {
					Boolean isBussinessRulesActive = agentAvailabilityService
							.validateBusinessRulesForAgentAssign(company, businessRules);
					if (chatEntries < company.getChatSettings().getMaxChatPerAgent() && chatStatus != null
							&& chatStatus.equalsIgnoreCase("available") && isBussinessRulesActive) {
						agentsWhoCanChat.add(key);
					}
				}
			}
		}

		if (agentsWhoCanChat.size() > 0) {
			int max = agentsWhoCanChat.size();
			int min = 1;
			int randomNumber = new Random().nextInt((max - min) + 1) + min;
			userId = agentsWhoCanChat.get(randomNumber - 1);
			Optional<Map<String, Object>> optionalUserEntry = entryRepository.findById(userId, "Users_" + companyId);
			agentUserEntry = optionalUserEntry.get();
		}

		if (agentUserEntry != null) {
			Optional<Map<String, Object>> optionalContactEntry = entryRepository
					.findById(agentUserEntry.get("CONTACT").toString(), "Contacts_" + companyId);
			String fullName = null;
			String agentFirstName = null;
			String agentLastName = null;

			if (optionalContactEntry.isPresent()) {
				fullName = optionalContactEntry.get().get("FULL_NAME").toString();
				agentFirstName = optionalContactEntry.get().get("FIRST_NAME").toString();
				agentLastName = optionalContactEntry.get().get("LAST_NAME").toString();
			}
			// Fetch the customer contactId
			String customerContactId = null;
			String customerName =null;
			if (customer != null) {
				Optional<Map<String, Object>> optionalCustomerContactEntry = entryRepository
						.findById(customer.get("CONTACT").toString(), "Contacts_" + companyId);
				if (optionalCustomerContactEntry.isPresent()) {
					customerContactId = optionalCustomerContactEntry.get().get("_id").toString();
					customerName = optionalCustomerContactEntry.get().get("FULL_NAME").toString();
				}
			}

			String agentRole = agentUserEntry.get("ROLE").toString();
			

			List<String> teams = (List<String>) agentUserEntry.get("TEAMS");
			Boolean isTeams = false;
			for (String teamId : teams) {
				if (teamsWhoCanChat.contains(teamId)) {
					isTeams = true;
				}
			}
			// Check the status and teams
			if (isTeams) {
				Optional<Map<String, Object>> optionalChatEntry = entryRepository
						.findBySessionUuid(chatUser.getSessionUUID(), "Chats_" + company.getId());
				if (optionalChatEntry.isPresent()) {
					Map<String, Object> existingChatEntry = optionalChatEntry.get();

					// If customer status is not equals to chatting
					String status = existingChatEntry.get("STATUS").toString();
					if (!status.equals("Chatting")) {

						// Add Discussion field to entry
						String discussionFieldName = null;
						if (optionalChatModule.isPresent()) {
							Module chatModule = optionalChatModule.get();
							List<ModuleField> fields = chatModule.getFields();
							for (ModuleField field : fields) {
								DataType dataType = field.getDataType();
								if (dataType.getDisplay().equalsIgnoreCase("Discussion")) {
									discussionFieldName = field.getName();
								}
							}

							String message = global.getFile("metadata_chat_agent_join.html");
							Map<String, Object> metaDataMessage = new HashMap<String, Object>();

							message = message.replace("NAME_REPLACE", fullName);
							message = message.replace("DATE_TIME_REPLACE", new SimpleDateFormat("MMM d y h:mm:ss a")
									.format(new Timestamp(new Date().getTime())));
							String systemUserUUID = getSystemUser(companyId);
							metaDataMessage.put("COMPANY_UUID", company.getCompanyUuid());
							metaDataMessage.put("MESSAGE_ID", UUID.randomUUID().toString());
							metaDataMessage.put("USER_UUID", systemUserUUID);
							DiscussionMessage discussionMessage = buildMetaDataPayload(message, companyId,
									chatModule.getModuleId(), existingChatEntry.get("_id").toString());
							if (discussionFieldName != null) {
								webSocketService.addDiscussionToEntry(discussionMessage, company.getCompanySubdomain(),
										agentUserEntry.get("_id").toString(), false);
							}
							
							
							
							

							// Add agents and requestor to entry
							List<Map<String, Object>> agents = new ArrayList<Map<String, Object>>();
							Map<String, Object> agent = new HashMap<String, Object>();
							agent.put("DATA_ID", agentUserEntry.get("_id").toString());
							agents.add(agent);
							HashMap<String, Object> chatEntry = new HashMap<String, Object>();
							Map<String, Object> requestor = new HashMap<String, Object>();
							requestor.put("DATA_ID", customerContactId);

							chatEntry.put("REQUESTOR", requestor);
							chatEntry.put("AGENTS", agents);
							chatEntry.put("DATA_ID", existingChatEntry.get("_id").toString());
							chatEntry.put("STATUS", "Chatting");

							Map<String, Object> updatedChatEntry = dataProxy.putModuleEntry(chatEntry,
									optionalChatModule.get().getModuleId(), false, companyId,
									customer.get("USER_UUID").toString());
							// Notify the agent that You have been assigned a new chat
							Notification notifyAgent = new Notification(company.getId(), chatModule.getModuleId(),
									existingChatEntry.get("_id").toString(), agentUserEntry.get("_id").toString(),
									new Date(), new Date(), false, "You have been assigned a new chat");
							redisTemplate.convertAndSend("notification", notifyAgent);
							 
						
							String messageForAgent = global.getFile("metadata_chat_agent_join.html");
							Map<String, Object> metaDataMessageForAgent = new HashMap<String, Object>();

							messageForAgent = messageForAgent.replace("NAME_REPLACE", customerName);
							messageForAgent = messageForAgent.replace("DATE_TIME_REPLACE", new SimpleDateFormat("MMM d y h:mm:ss a")
									.format(new Timestamp(new Date().getTime())));
							String systemUserUUIDForCustomer = getSystemUser(companyId);
							metaDataMessageForAgent.put("COMPANY_UUID", company.getCompanyUuid());
							metaDataMessageForAgent.put("MESSAGE_ID", UUID.randomUUID().toString());
							metaDataMessageForAgent.put("USER_UUID", systemUserUUIDForCustomer);
							DiscussionMessage discussionMessageForAgent = buildMetaDataPayload(messageForAgent, companyId,
									chatModule.getModuleId(), existingChatEntry.get("_id").toString());
							if (discussionFieldName != null) {
								webSocketService.addDiscussionToEntry(discussionMessageForAgent, company.getCompanySubdomain(),
										customer.get("_id").toString(), false);
							}

							
							
							AgentDetails agentDetails = new AgentDetails(companyId, agentFirstName, agentLastName,
									agentUserEntry.get("_id").toString(), customer.get("_id").toString(), true,
									chatUser.getSessionUUID(), new Date(), agentRole, customerRole,
									customer.get("USER_UUID").toString(), existingChatEntry.get("_id").toString());

							ChatNotification chatNotification = new ChatNotification(companyId, "CHAT_NOTIFICATION",
									chatUser.getSessionUUID(), updatedChatEntry, "Chatting", agentDetails);
							addToChatNotificationQueue(chatNotification);

						}
					}
				}
			}
		} else {
			AgentDetails agentDetails = new AgentDetails(companyId, null, null, null, customer.get("_id").toString(),
					false, chatUser.getSessionUUID(), null, null, customerRole, customer.get("USER_UUID").toString(),
					null);

			ChatNotification chatNotification = new ChatNotification(companyId, "CHAT_NOTIFICATION",
					chatUser.getSessionUUID(), null, "Browsing", agentDetails);
			addToChatNotificationQueue(chatNotification);

		}
	}

	public String getSystemUser(String companyId) {
		String UUID = "";
		try {

			Optional<Map<String, Object>> optionalUser = entryRepository.findUserByEmailAddress("system@ngdesk.com",
					"Users_" + companyId);
			UUID = optionalUser.get().get("USER_UUID").toString();

		} catch (Exception e) {
			e.printStackTrace();
		}
		return UUID;
	}

	public DiscussionMessage buildMetaDataPayload(String message, String companyId, String moduleId, String dataId) {

		Optional<Map<String, Object>> optionalUser = entryRepository.findUserByEmailAddress("system@ngdesk.com",
				"Users_" + companyId);

		Map<String, Object> systemUser = optionalUser.get();

		String contactId = systemUser.get("CONTACT").toString();

		Optional<Map<String, Object>> optionalContact = entryRepository.findById(contactId, "Contacts_" + companyId);
		Map<String, Object> contact = optionalContact.get();

		Sender sender = new Sender(contact.get("FIRST_NAME").toString(), contact.get("LAST_NAME").toString(),
				systemUser.get("USER_UUID").toString(), systemUser.get("ROLE").toString());

		return new DiscussionMessage(message, new Date(), UUID.randomUUID().toString(), "META_DATA",
				new ArrayList<MessageAttachment>(), sender, moduleId, dataId, null);

	}

	public void addToChatNotificationQueue(ChatNotification message) {
		redisTemplateForChatNotification.convertAndSend("chat_notification", message);
	}

}
