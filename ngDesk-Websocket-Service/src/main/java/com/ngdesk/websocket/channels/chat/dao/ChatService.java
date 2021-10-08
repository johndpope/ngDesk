package com.ngdesk.websocket.channels.chat.dao;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
import com.ngdesk.data.dao.DiscussionMessage;
import com.ngdesk.repositories.ChatChannelRepository;
import com.ngdesk.repositories.CompaniesRepository;
import com.ngdesk.repositories.ModuleEntryRepository;
import com.ngdesk.repositories.ModulesRepository;
import com.ngdesk.websocket.companies.dao.Company;
import com.ngdesk.websocket.modules.dao.Module;
import com.ngdesk.websocket.notification.dao.AgentDetails;

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
								chatNotification.setSessionUUID(pageLoad.getSessionUUID());
								chatNotification.setStatus("Browsing");
								if (optionalChatEntry.isEmpty()) {
									entry.put("STATUS", "Browsing");
									chatEntry = dataProxy.postModuleEntry(entry, optionalChatModule.get().getModuleId(),
											false, companyId, user.get("USER_UUID").toString());
									chatNotification.setType("CHAT_NOTIFICATION");

								} else {
									entry.put("DATA_ID", optionalChatEntry.get().get("_id").toString());
									chatEntry = dataProxy.putModuleEntry(entry, optionalChatModule.get().getModuleId(),
											false, companyId, user.get("USER_UUID").toString());
									chatNotification.setType("CHAT_NOTIFICATION");
									AgentDetails agentDetails = getAgentDetails(optionalChatEntry.get(), companyId);
									chatNotification.setAgentDetails(agentDetails);
									if (agentDetails != null && agentDetails.getAgentDataId() != null) {
										chatNotification.setStatus("Chatting");
									}

								}
								chatNotification.setEntry(chatEntry);
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

	public AgentDetails getAgentDetails(Map<String, Object> chatEntry, String companyId) {

		List<String> agents = (List<String>) chatEntry.get("AGENTS");
		if (agents != null) {

			Optional<Map<String, Object>> optionalUserEntry = entryRepository.findById(agents.get(0),
					"Users_" + companyId);

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
					Optional<Map<String, Object>> optionalCustomerContactEntry = entryRepository.findById(customerId,
							"Contacts_" + companyId);
					if (optionalCustomerContactEntry.isPresent()) {

						Map<String, Object> customerContactEntry = optionalCustomerContactEntry.get();
						Optional<Map<String, Object>> optionalCustomerUserEntry = entryRepository
								.findById(customerContactEntry.get("USER").toString(), "Users_" + companyId);
						if (optionalCustomerUserEntry.isPresent()) {
							Map<String, Object> customer = optionalCustomerUserEntry.get();
							String customerRole = customer.get("ROLE").toString();
							Date agentAssignedTime = fetchAgentAssignedTime(chatEntry);

							AgentDetails agentDetails = new AgentDetails(companyId, agentFirstName, agentLastName,
									agentUserEntry.get("_id").toString(), customer.get("_id").toString(), true,
									chatEntry.get("SESSION_UUID").toString(), agentAssignedTime, agentRole,
									customerRole, customer.get("USER_UUID").toString(),
									chatEntry.get("_id").toString());
							return agentDetails;
						}

					}
				}
			}

		}
		return new AgentDetails();
	}

	public Date fetchAgentAssignedTime(Map<String, Object> chatEntry) {
		try {
			List<DiscussionMessage> chats = (List<DiscussionMessage>) chatEntry.get("CHAT");
			List<Date> assignedDates = new ArrayList<Date>();
			if (chats != null) {
				for (DiscussionMessage chat : chats) {
					if (chat.getMessageType().equals("META_DATA")
							&& chat.getMessage().contains("has joined the chat")) {
						assignedDates.add(chat.getDateCreated());
					}
				}
				if (assignedDates.size() > 0) {
					return assignedDates.get(assignedDates.size() - 1);
				}
				return new Date();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new Date();

	}

}
