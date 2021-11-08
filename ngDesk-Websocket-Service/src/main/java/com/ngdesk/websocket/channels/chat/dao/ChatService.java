package com.ngdesk.websocket.channels.chat.dao;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.TimeZone;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ngdesk.commons.Global;
import com.ngdesk.commons.mail.SendMail;
import com.ngdesk.data.dao.DiscussionMessage;
import com.ngdesk.data.dao.Sender;
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
	RedisTemplate<String, ChatTicketStatusMessage> redisTemplateForChatTicketStatusMessage;

	@Autowired
	RedisTemplate<String, ChatNotification> redisTemplateForChatNotification;

	@Autowired
	RedisTemplate<String, ChatVisitedPagesNotification> redisTemplateForChatVisitedPagesNotification;

	@Autowired
	Global global;

	@Autowired
	SendMail sendMail;

	public void publishPageLoad(ChatWidgetPayload pageLoad) {
		try {
			if (pageLoad.getSubdomain() != null) {
				Optional<Company> optionalCompany = companiesRepository.findCompanyBySubdomain(pageLoad.getSubdomain());
				if (optionalCompany.isPresent()) {
					Company company = optionalCompany.get();
					String companyId = company.getId();
					Optional<Module> optionalChatModule = modulesRepository.findModuleByName("Chats",
							"modules_" + companyId);
					if (optionalChatModule.isPresent()) {
						Optional<Map<String, Object>> optionalChatEntry = moduleEntryRepository
								.findBySessionUuid(pageLoad.getSessionUUID(), "Chats_" + companyId);
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

	// Send chat transcript to mail
	public void sendChatTranscript(CloseChat closeChat) {
		try {
			String subdomain = closeChat.getSubdomain();
			Optional<Company> optionalCompany = companiesRepository.findCompanyBySubdomain(subdomain);
			if (optionalCompany.isPresent()) {
				Company company = optionalCompany.get();
				String companyId = company.getId();

				Optional<Map<String, Object>> optionalChatEntry = moduleEntryRepository
						.findBySessionUuid(closeChat.getSessionUUID(), "Chats_" + companyId);

				if (optionalChatEntry.isPresent()) {
					Map<String, Object> chatEntry = optionalChatEntry.get();

					Optional<Map<String, Object>> optionalContactEntry = moduleEntryRepository
							.findById(chatEntry.get("REQUESTOR").toString(), "Contacts_" + companyId);

					if (closeChat.getIsSendChatTranscript()) {
						String messageChat = "";
						String companyTimezone = "UTC";
						if (!company.getTimezone().isEmpty()) {
							companyTimezone = company.getTimezone();
						}
						List<DiscussionMessage> chats = (List<DiscussionMessage>) chatEntry.get("CHAT");
						for (DiscussionMessage chat : chats) {
							String chatWithoutHtml = chat.getMessage();
							Sender sender = chat.getSender();

							Date chatCreatedTime = chat.getDateCreated();
							SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy hh:mm a");
							TimeZone tz = TimeZone.getTimeZone(companyTimezone);
							formatter.setTimeZone(tz);
							String chatDisplayDateTime = formatter.format(chatCreatedTime);

							if (chat.getMessageType().equals("META_DATA")) {
								if (messageChat.length() != 0) {
									messageChat = messageChat + "<br/>" + "(" + chatDisplayDateTime + ")" + " "
											+ "<div class='mat-caption' style='color: #68737D; font-weight: 500;'>"
											+ chatWithoutHtml + "</div>";
								} else {
									messageChat = "(" + chatDisplayDateTime + ")" + " "
											+ "<div class='mat-caption' style='color: #68737D; font-weight: 500;'>"
											+ chatWithoutHtml + "</div>";
								}
							}
							if (chat.getMessageType().equals("MESSAGE")) {
								if (messageChat.length() == 0) {
									messageChat = "(" + chatDisplayDateTime + ")" + " " + sender.getFirstName() + ": "
											+ chatWithoutHtml + "<br/>";
								} else {
									messageChat = messageChat + "<br/>" + "(" + chatDisplayDateTime + ")" + " "
											+ sender.getFirstName() + ": " + chatWithoutHtml + "<br/>";
								}
							}
						}

						if (optionalContactEntry.isPresent()) {
							String chatTranscipt = global.getFile("chat_transcript.html");
							String userName = optionalContactEntry.get().get("FULL_NAME").toString();
							chatTranscipt = chatTranscipt.replace("NAME_REPLACE", userName);
							chatTranscipt = chatTranscipt.replace("CHAT_HISTORY_REPLACE", messageChat); // FETCHING DATA
							Optional<Map<String, Object>> optionalUserEntry = moduleEntryRepository
									.findById(optionalContactEntry.get().get("USER").toString(), "Users_" + companyId);
							if (optionalUserEntry.isPresent()) {
								String to = optionalUserEntry.get().get("EMAIL_ADDRESS").toString();
								String from = "support@" + subdomain + ".ngdesk.com";
								String subject = "Chat Transcript from ngDesk";
								String body = chatTranscipt;
								sendMail.send(to, from, subject, body);
								setStatusOffline(company, optionalContactEntry, chatEntry);
								ChatTicketStatusMessage chatTicketStatusMessage = new ChatTicketStatusMessage(companyId,
										closeChat.getSessionUUID(), "CLOSE_SESSION", "CHAT_ENDED_FROM_CHATTING",
										"CUSTOMER_HAS_ENDED_THE_CHAT");
								addToChatTicketStatusQueue(chatTicketStatusMessage);

							}
						}
					} else {
						if (closeChat.getIsAgentCloseChat()) {
							setStatusBrowsing(company, optionalContactEntry, chatEntry);
						} else {
							setStatusOffline(company, optionalContactEntry, chatEntry);
							ChatTicketStatusMessage chatTicketStatusMessage = new ChatTicketStatusMessage(companyId,
									closeChat.getSessionUUID(), "CLOSE_SESSION", "CHAT_ENDED_FROM_CHATTING",
									"CUSTOMER_HAS_ENDED_THE_CHAT");
							addToChatTicketStatusQueue(chatTicketStatusMessage);
						}
					}
				}
			}
		} catch (Exception e) {

		}
	}

	public void setStatusBrowsing(Company company, Optional<Map<String, Object>> optionalContactEntry,
			Map<String, Object> chatEntry) {

		Optional<Module> optionalChatModule = modulesRepository.findModuleByName("Chats", "modules_" + company.getId());

		if (optionalChatModule.isPresent()) {
			if (optionalContactEntry.isPresent()) {
				Optional<Map<String, Object>> optionalUserEntry = moduleEntryRepository
						.findById(optionalContactEntry.get().get("USER").toString(), "Users_" + company.getId());
				if (optionalUserEntry.isPresent()) {
					HashMap<String, Object> updateChatEntry = new HashMap<String, Object>();
					updateChatEntry.put("DATA_ID", chatEntry.get("_id").toString());
					updateChatEntry.put("STATUS", "Browsing");
					dataProxy.putModuleEntry(updateChatEntry, optionalChatModule.get().getModuleId(), false,
							company.getId(), optionalUserEntry.get().get("USER_UUID").toString());
				}
			}
		}

	}

	public void setStatusOffline(Company company, Optional<Map<String, Object>> optionalContactEntry,
			Map<String, Object> chatEntry) {

		Optional<Module> optionalChatModule = modulesRepository.findModuleByName("Chats", "modules_" + company.getId());

		if (optionalChatModule.isPresent()) {
			if (optionalContactEntry.isPresent()) {
				Optional<Map<String, Object>> optionalUserEntry = moduleEntryRepository
						.findById(optionalContactEntry.get().get("USER").toString(), "Users_" + company.getId());
				if (optionalUserEntry.isPresent()) {
					HashMap<String, Object> updateChatEntry = new HashMap<String, Object>();
					updateChatEntry.put("DATA_ID", chatEntry.get("_id").toString());
					updateChatEntry.put("STATUS", "Offline");
					dataProxy.putModuleEntry(updateChatEntry, optionalChatModule.get().getModuleId(), false,
							company.getId(), optionalUserEntry.get().get("USER_UUID").toString());
				}
			}
		}

	}

	public void addToChatTicketStatusQueue(ChatTicketStatusMessage chatTicketStatusMessage) {
		redisTemplateForChatTicketStatusMessage.convertAndSend("chat_ticket_status", chatTicketStatusMessage);
	}

	public void updateChatVistedPages(ChatVisitedPages chatVisitedPages) {
		String subdomain = chatVisitedPages.getCompanySubdomain();
		Optional<Company> optionalCompany = companiesRepository.findCompanyBySubdomain(subdomain);
		if (optionalCompany.isPresent()) {
			Company company = optionalCompany.get();
			String companyId = company.getId();

			Optional<Map<String, Object>> optionalChatEntry = moduleEntryRepository
					.findBySessionUuid(chatVisitedPages.getSessionUUID(), "Chats_" + companyId);
			Optional<Module> optionalChatModule = modulesRepository.findModuleByName("Chats", "modules_" + companyId);

			if (optionalChatModule.isPresent()) {
				if (optionalChatEntry.isPresent()) {
					Map<String, Object> chatEntry = optionalChatEntry.get();
					Optional<Map<String, Object>> optionalContactEntry = moduleEntryRepository
							.findById(chatEntry.get("REQUESTOR").toString(), "Contacts_" + companyId);
					if (optionalContactEntry.isPresent()) {
						Optional<Map<String, Object>> optionalUserEntry = moduleEntryRepository.findById(
								optionalContactEntry.get().get("USER").toString(), "Users_" + company.getId());
						if (optionalUserEntry.isPresent()) {
							HashMap<String, Object> updateChatEntry = new HashMap<String, Object>();
							updateChatEntry.put("DATA_ID", chatEntry.get("_id").toString());
							List<String> visitedPages = new ArrayList<String>();
							if (chatEntry.get("PAGES_VISITED") != null) {
								visitedPages = (List<String>) chatEntry.get("PAGES_VISITED");
							}
							if (chatVisitedPages.getVisitedPages() != null) {
								visitedPages.add(chatVisitedPages.getVisitedPages());
							}
							updateChatEntry.put("PAGES_VISITED", visitedPages);
							Map<String, Object> updatedChatEntry = dataProxy.putModuleEntry(updateChatEntry,
									optionalChatModule.get().getModuleId(), false, companyId,
									optionalUserEntry.get().get("USER_UUID").toString());
							List<String> agents = (List<String>) updatedChatEntry.get("AGENTS");
							ChatVisitedPagesNotification chatVisitedPagesNotification = new ChatVisitedPagesNotification(
									(List<String>) updatedChatEntry.get("PAGES_VISITED"),
									chatVisitedPages.getSessionUUID(), agents.get(0), "VISITED_PAGES", companyId);

							redisTemplateForChatVisitedPagesNotification.convertAndSend("chat_visited_pages",
									chatVisitedPagesNotification);

						}
					}
				}
			}
		}
	}
}
