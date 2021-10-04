package com.ngdesk.websocket.channels.chat.dao;

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
import com.ngdesk.websocket.companies.dao.Company;
import com.ngdesk.websocket.dao.WebSocketService;
import com.ngdesk.websocket.modules.dao.DataType;
import com.ngdesk.websocket.modules.dao.Module;
import com.ngdesk.websocket.modules.dao.ModuleField;
import com.ngdesk.websocket.notification.dao.Notification;
import com.ngdesk.websocket.notification.dao.NotificationOfAgentDetails;

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
	RedisTemplate<String, NotificationOfAgentDetails> redisTemplateNotificationOfAgentDetails;

	@Autowired
	Global global;

	@Autowired
	WebSocketService webSocketService;

	@Autowired
	DataProxy dataProxy;

	public void assignChatToAgent(Company company, ChatUser chatUser, Map<String, Object> customer) {
		String companyId = company.getId();
		Optional<Map<String, Object>> optionalCustomerRoleEntry = entryRepository
				.findById(customer.get("ROLE").toString(), "roles_" + companyId);
		String customerRole = null;
		if (optionalCustomerRoleEntry.isPresent()) {
			customerRole = optionalCustomerRoleEntry.get().get("_id").toString();
		}
		Optional<Module> optionalChatModule = modulesRepository.findModuleByName("Chat", "modules_" + company.getId());
		List<String> teamsWhoCanChat = company.getChatSettings().getTeamsWhoCanChat();
		ConcurrentHashMap<String, UserSessions> sessionMap = sessionService.sessions.get(company.getCompanySubdomain());
		String userId = null;

		Map<String, Object> agentUserEntry = null;
		for (String keySet : sessionMap.keySet()) {

			userId = keySet;
			UserSessions userSessions = sessionMap.get(userId);
			String chatStatus = userSessions.getChatStatus();
			Optional<Map<String, Object>> optionalUserEntry = entryRepository.findById(userId, "Users_" + companyId);
			if (optionalUserEntry.isPresent() && chatStatus != null) {
				if (chatStatus.equalsIgnoreCase("available")) {
					Integer chatEntries = entryRepository.findByAgentAndCollectionName(userId.toString(),
							"Chat_" + company.getId());
					if (chatEntries <= 5) {

						agentUserEntry = optionalUserEntry.get();
					}
				}
			}
		}
		if (agentUserEntry != null) {
			Optional<Map<String, Object>> optionalContactEntry = entryRepository
					.findById(agentUserEntry.get("CONTACT").toString(), "Contacts_" + companyId);
			String fullName = null;
			String contactId = null;
			String agentFirstName = null;
			String agentLastName = null;

			if (optionalContactEntry.isPresent()) {
				fullName = optionalContactEntry.get().get("FULL_NAME").toString();
				agentFirstName = optionalContactEntry.get().get("FIRST_NAME").toString();
				agentLastName = optionalContactEntry.get().get("LAST_NAME").toString();
				contactId = optionalContactEntry.get().get("_id").toString();
			}

			Optional<Map<String, Object>> optionalRoleEntry = entryRepository
					.findById(agentUserEntry.get("ROLE").toString(), "Roles_" + companyId);
			String agentRole = null;
			if (optionalRoleEntry.isPresent()) {
				agentRole = optionalRoleEntry.get().get("_id").toString();
			}

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
						.findBySessionUuid(chatUser.getSessionUUID(), "Chat_" + company.getId());
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
										customer.get("_id").toString(), false);
							}

							// Add agents and requestor to entry
							List<Map<String, Object>> agents = new ArrayList<Map<String, Object>>();
							Map<String, Object> agent = new HashMap<String, Object>();
							agent.put("DATA_ID", agentUserEntry.get("_id").toString());
							agents.add(agent);
							HashMap<String, Object> chatEntry = new HashMap<String, Object>();
							Map<String, Object> requestor = new HashMap<String, Object>();
							requestor.put("DATA_ID", contactId);

							chatEntry.put("REQUESTOR", requestor);
							chatEntry.put("AGENTS", agents);
							chatEntry.put("DATA_ID", existingChatEntry.get("_id").toString());
							chatEntry.put("STATUS", "Chatting");
							dataProxy.putModuleEntry(chatEntry, optionalChatModule.get().getModuleId(), false,
									companyId, customer.get("USER_UUID").toString());
							// Notify the agent that You have been assigned a new chat
							Notification notifyAgent = new Notification(company.getId(), chatModule.getModuleId(),
									customer.get("_id").toString(), agentUserEntry.get("_id").toString(), new Date(),
									new Date(), false, "You have been assigned a new chat");
							redisTemplate.convertAndSend("notification", notifyAgent);

							NotificationOfAgentDetails notificationOfAgentDetails = new NotificationOfAgentDetails(
									companyId, agentFirstName, agentLastName, agentUserEntry.get("_id").toString(),
									customer.get("_id").toString(), true, chatUser.getSessionUUID(), "AGENTS_DATA",
									new Date(), agentRole, customerRole, customer.get("USER_UUID").toString(),
									existingChatEntry.get("_id").toString());
							redisTemplateNotificationOfAgentDetails.convertAndSend("agents_available",
									notificationOfAgentDetails);

						}
					}
				}
			}
		} else {
			NotificationOfAgentDetails notificationOfAgentDetails = new NotificationOfAgentDetails(companyId, null,
					null, null, customer.get("_id").toString(), false, chatUser.getSessionUUID(), "AGENTS_DATA", null,
					null, customerRole, customer.get("USER_UUID").toString(), null);
			redisTemplateNotificationOfAgentDetails.convertAndSend("agents_available", notificationOfAgentDetails);

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

}
