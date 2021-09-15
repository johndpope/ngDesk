package com.ngdesk.websocket.dao;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ngdesk.data.dao.DiscussionMessage;
import com.ngdesk.data.dao.Sender;
import com.ngdesk.data.dao.WorkflowPayload;
import com.ngdesk.repositories.ChatChannelRepository;
import com.ngdesk.repositories.CompaniesRepository;
import com.ngdesk.repositories.DnsRepository;
import com.ngdesk.repositories.ModuleEntryRepository;
import com.ngdesk.repositories.ModulesRepository;
import com.ngdesk.websocket.UserSessions;
import com.ngdesk.websocket.SessionService;
import com.ngdesk.websocket.companies.dao.ChatSettingsMessage;
import com.ngdesk.websocket.companies.dao.Company;
import com.ngdesk.websocket.companies.dao.DnsRecord;
import com.ngdesk.websocket.modules.dao.Module;
import com.ngdesk.websocket.modules.dao.ModuleField;
import com.ngdesk.websocket.modules.dao.ModuleService;
import com.ngdesk.websocket.roles.dao.RolesService;

@Component
public class WebSocketService {

	@Autowired
	ModuleEntryRepository entryRepository;

	@Autowired
	ModulesRepository modulesRepository;

	@Autowired
	CompaniesRepository companiesRepository;

	@Autowired
	ModuleService moduleService;

	@Autowired
	RolesService rolesService;

	@Autowired
	SessionService sessionService;

	@Autowired
	DnsRepository dnsRepository;

	@Autowired
	ChatChannelRepository chatChannelRepository;

	@Autowired
	RedissonClient redisson;

	@Autowired
	RabbitTemplate rabbitTemplate;

	private final Logger log = LoggerFactory.getLogger(WebSocketService.class);

	public void addDiscussionToEntry(DiscussionMessage message, String subdomain, String userId, boolean isTrigger) {
		try {
			Optional<Company> optionalCompany = companiesRepository.findCompanyBySubdomain(subdomain);
			if (optionalCompany.isPresent()) {

				Company company = optionalCompany.get();

				Optional<Map<String, Object>> optionalUser = entryRepository.findEntryById(userId,
						"Users_" + company.getId());

				if (optionalUser.isPresent()) {

					Map<String, Object> user = optionalUser.get();

					String contactId = user.get("CONTACT").toString();
					Optional<Map<String, Object>> optionalContact = entryRepository.findEntryById(contactId,
							"Contacts_" + company.getId());

					if (optionalContact.isPresent()) {

						Map<String, Object> contact = optionalContact.get();

						String firstName = contact.get("FIRST_NAME").toString();
						String lastName = contact.get("LAST_NAME").toString();

						Optional<Module> optionalModule = modulesRepository.findById(message.getModuleId(),
								"modules_" + company.getId());

						if (optionalModule.isPresent()) {
							Module module = optionalModule.get();

							ModuleField discussionField = module.getFields().stream()
									.filter(field -> field.getDataType().getDisplay().equals("Discussion")).findFirst()
									.orElse(null);
							if (discussionField != null) {
								Optional<Map<String, Object>> optionalEntry = entryRepository.findEntryById(
										message.getDataId(),
										moduleService.getCollectionName(module.getName(), company.getId()));

								if (optionalEntry.isPresent()) {

									Map<String, Object> entry = optionalEntry.get();
									if (message.getMessageId() == null || message.getMessageId().isBlank()) {
										message.setMessageId(UUID.randomUUID().toString());
									}

									Sender sender = new Sender(firstName, lastName, user.get("USER_UUID").toString(),
											user.get("ROLE").toString());
									message.setSender(sender);
									message.setDateCreated(new Date());

									entryRepository.addDiscussionToEntry(message, discussionField.getName(),
											entry.get("_id").toString(),
											moduleService.getCollectionName(module.getName(), company.getId()));
									if (!isTrigger) {
										WorkflowPayload workflowPayload = new WorkflowPayload(
												user.get("_id").toString(), module.getModuleId(), company.getId(),
												entry.get("_id").toString(), entry, "PUT", new Date());
										addToQueue(workflowPayload);
									}

									// PUBLISH PAYLOAD TO USERS
									if (message.getMessageType() != null
											&& !message.getMessageType().equals("META_DATA")) {
										publishDiscussionToUsersInvolved(company, message, entry);
									}
								}
							}
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public String getSubdomain(String host) {
		String subdomain = null;
		try {
			if (host.contains("localhost")) {
				subdomain = "dev1";
			} else if (host.endsWith("ngdesk.com")) {
				try {
					subdomain = host.split("\\.")[0];
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				// CNAME
				Optional<DnsRecord> optionalRecord = dnsRepository.getDnsRecordByCname(host);
				if (optionalRecord.isPresent()) {
					return optionalRecord.get().getSubdomain();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return subdomain;
	}

	public void addToQueue(WorkflowPayload workflowPayload) {
		try {
			log.debug("Publishing to manager");
			log.debug(new ObjectMapper().writeValueAsString(workflowPayload));

			rabbitTemplate.convertAndSend("execute-module-workflows", workflowPayload);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void notifyUsersToUpdateInModule(Company company, NotificationMessage message) {
		String companyId = company.getId();
		ObjectMapper mapper = new ObjectMapper();

		message.setType("NOTIFICATION");

		if (sessionService.sessions.containsKey(company.getCompanySubdomain())) {
			ConcurrentHashMap<String, UserSessions> sessions = sessionService.sessions
					.get(company.getCompanySubdomain());
			for (String userId : sessions.keySet()) {

				Optional<Map<String, Object>> optionalUser = entryRepository.findEntryById(userId,
						"Users_" + companyId);

				if (optionalUser.isPresent()) {
					Map<String, Object> user = optionalUser.get();
					if (rolesService.isAuthorizedForRecord(user.get("ROLE").toString(), "GET", message.getModuleId(),
							company.getId())) {
						ConcurrentLinkedQueue<WebSocketSession> userSessions = sessions.get(userId).getSessions();
						userSessions.forEach(session -> {
							try {
								String payload = mapper.writeValueAsString(message);
								session.sendMessage(new TextMessage(payload));
							} catch (JsonProcessingException e) {
								e.printStackTrace();
							} catch (IOException e) {
								e.printStackTrace();
							} catch (IllegalStateException e) {
								e.printStackTrace();
								userSessions.remove(session);
							}
						});
					}
				}
			}
		}
	}

	public void publishDiscussionToUsersInvolved(Company company, DiscussionMessage message,
			Map<String, Object> entry) {

		String companyId = company.getId();
		ObjectMapper mapper = new ObjectMapper();

		message.setType("DISCUSSION");

		List<String> teamIds = (List<String>) entry.get("TEAMS");
		List<Map<String, Object>> teams = entryRepository.findTeamsByIds(teamIds, "Teams_" + companyId);

		List<String> authorizedUsers = new ArrayList<String>();

		teams.forEach(team -> {
			if (team.get("USERS") != null) {
				authorizedUsers.addAll((List<String>) team.get("USERS"));
			}
		});

		if (!sessionService.sessions.containsKey(company.getCompanySubdomain())) {
			return;
		}

		ConcurrentHashMap<String, UserSessions> sessions = sessionService.sessions.get(company.getCompanySubdomain());

		for (String userId : sessions.keySet()) {

			Optional<Map<String, Object>> optionalUser = entryRepository.findEntryById(userId, "Users_" + companyId);

			if (optionalUser.isPresent()) {
				Map<String, Object> user = optionalUser.get();
				if (rolesService.isAuthorizedForRecord(user.get("ROLE").toString(), "GET", message.getModuleId(),
						company.getId())) {
					if (!rolesService.isSystemAdmin(company.getId(), user.get("ROLE").toString())
							&& !authorizedUsers.contains(userId)) {
						continue;
					}

					ConcurrentLinkedQueue<WebSocketSession> userSessions = sessions.get(userId).getSessions();
					userSessions.forEach(session -> {
						try {
							String payload = mapper.writeValueAsString(message);
							session.sendMessage(new TextMessage(payload));
						} catch (JsonProcessingException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						} catch (IllegalStateException e) {
							e.printStackTrace();
							userSessions.remove(session);
						}
					});
				}
			}
		}
	}

	public void publishChatSettings(Company company, ChatSettingsMessage message) {

		String companyId = company.getId();
		ObjectMapper mapper = new ObjectMapper();

		if (!sessionService.sessions.containsKey(company.getCompanySubdomain())) {
			return;
		}

		ConcurrentHashMap<String, UserSessions> sessions = sessionService.sessions.get(company.getCompanySubdomain());

		for (String userId : sessions.keySet()) {
			Optional<Map<String, Object>> optionalUser = entryRepository.findEntryById(userId, "Users_" + companyId);

			if (optionalUser.isPresent()) {
				Map<String, Object> user = optionalUser.get();
				if (!rolesService.isSystemAdmin(company.getId(), user.get("ROLE").toString())) {
					continue;
				}
				ConcurrentLinkedQueue<WebSocketSession> userSessions = sessions.get(userId).getSessions();
				userSessions.forEach(session -> {
					try {
						String payload = mapper.writeValueAsString(message);
						session.sendMessage(new TextMessage(payload));
					} catch (JsonProcessingException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					} catch (IllegalStateException e) {
						e.printStackTrace();
						userSessions.remove(session);
					}
				});

			}

		}

	}

}
