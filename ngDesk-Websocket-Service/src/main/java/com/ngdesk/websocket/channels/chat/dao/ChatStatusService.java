package com.ngdesk.websocket.channels.chat.dao;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import com.ngdesk.repositories.CompaniesRepository;
import com.ngdesk.repositories.ModuleEntryRepository;
import com.ngdesk.websocket.SessionService;
import com.ngdesk.websocket.UserSessions;
import com.ngdesk.websocket.companies.dao.ChatBusinessRules;
import com.ngdesk.websocket.companies.dao.Company;

@Component
public class ChatStatusService {

	@Autowired
	SessionService sessionService;

	@Autowired
	CompaniesRepository companiesRepository;

	@Autowired
	RedisTemplate<String, ChatStatusMessage> redisTemplate;

	@Autowired
	RedisTemplate<String, AgentChattingStatusCheck> redisTemplateChatAgentStatusCheck;

	@Autowired
	AgentAvailabilityService agentAvailabilityService;

	@Autowired
	RabbitTemplate rabbitTemplate;

	@Autowired
	ModuleEntryRepository entryRepository;

	public void updateChatStatus(ChatStatus chatStatus) {
		if (chatStatus.getSubdomain() != null) {
			Optional<Company> optionalComapny = companiesRepository.findCompanyBySubdomain(chatStatus.getSubdomain());
			if (optionalComapny.isPresent()) {
				Company company = optionalComapny.get();
				ConcurrentHashMap<String, UserSessions> sessionMap = sessionService.sessions
						.get(company.getCompanySubdomain());
				UserSessions userSessions = sessionMap.get(chatStatus.getUserId());
				ChatStatusMessage chatStatusMessage = new ChatStatusMessage();
				if (chatStatus.isAccepting()) {
					isAgentsAvailable(sessionMap, company);
					userSessions.setChatStatus("available");
					chatStatusMessage.setChatStatus("available");
					chatStatusMessage.setCompanyId(company.getId());
					chatStatusMessage.setType("CHAT_STATUS");
					chatStatusMessage.setUserId(chatStatus.getUserId());
					addToQueue(chatStatusMessage);

				} else {
					Integer chatEntries = entryRepository.findByAgentAndCollectionName(chatStatus.getUserId(),
							"Chats_" + company.getId());
					if (chatEntries > 0) {
						AgentChattingStatusCheck agentChattingStatusCheck = new AgentChattingStatusCheck(
								chatStatus.getUserId(), company.getCompanySubdomain(), "chatAgentStatus",
								"You have active chat users");
						addToQueueChatAgentStatusCheck(agentChattingStatusCheck);
					} else {
						userSessions.setChatStatus("not available");
						chatStatusMessage.setChatStatus("not available");
						chatStatusMessage.setCompanyId(company.getId());
						chatStatusMessage.setType("CHAT_STATUS");
						chatStatusMessage.setUserId(chatStatus.getUserId());
						addToQueue(chatStatusMessage);
					}
				}
				sessionMap.put(chatStatus.getUserId(), userSessions);
				sessionService.sessions.put(company.getCompanySubdomain(), sessionMap);
			}
		}

	}

	public void addToQueue(ChatStatusMessage chatStatusMessage) {
		redisTemplate.convertAndSend("chat_status", chatStatusMessage);
	}

	public void addToQueueChatAgentStatusCheck(AgentChattingStatusCheck agentChattingStatusCheck) {
		redisTemplateChatAgentStatusCheck.convertAndSend("chat_agent_status_check", agentChattingStatusCheck);
	}

	public void publishOnChatStatusCheck(ChatStatusCheck chatStatusCheck) {
		if (chatStatusCheck.getSubdomain() != null) {
			Optional<Company> optionalComapny = companiesRepository
					.findCompanyBySubdomain(chatStatusCheck.getSubdomain());
			if (optionalComapny.isPresent()) {
				Company company = optionalComapny.get();
				ConcurrentHashMap<String, UserSessions> sessionMap = sessionService.sessions
						.get(company.getCompanySubdomain());
				UserSessions userSessions = sessionMap.get(chatStatusCheck.getUserId());
				ChatStatusMessage chatStatusMessage = new ChatStatusMessage();
				chatStatusMessage.setChatStatus(userSessions.getChatStatus());
				chatStatusMessage.setCompanyId(company.getId());
				chatStatusMessage.setType("CHAT_STATUS");
				chatStatusMessage.setUserId(chatStatusCheck.getUserId());
				addToQueue(chatStatusMessage);
			}
		}

	}

	public void addAgentAvailabilityQueue(String subdomain) {
		rabbitTemplate.convertAndSend("publish-agent-availability", subdomain);
	}

	public void isAgentsAvailable(ConcurrentHashMap<String, UserSessions> sessionMap, Company company) {
		ChatBusinessRules businessRules = company.getChatSettings().getChatBusinessRules();
		Boolean hasRestriction = company.getChatSettings().getHasRestrictions();
		boolean isAgentsavailable = false;
		for (String key : sessionMap.keySet()) {
			UserSessions userSessions = sessionMap.get(key);
			if (!userSessions.getSessions().isEmpty()) {
				String queryParams = userSessions.getSessions().element().getUri().getQuery();
				if (queryParams.contains("authentication_token")) {
					String chatStatus = userSessions.getChatStatus();
					if (hasRestriction == null || !hasRestriction) {
						if (chatStatus != null && chatStatus.equalsIgnoreCase("available")) {
							isAgentsavailable = true;
						}
					} else if (hasRestriction) {
						boolean isBusinessHoursActive = agentAvailabilityService
								.validateBusinessRulesForAgentAssign(company, businessRules);
						if (chatStatus != null && chatStatus.equalsIgnoreCase("available") && isBusinessHoursActive) {
							isAgentsavailable = true;
						}
					}
				}
			}
		}
		if (!isAgentsavailable) {
			addAgentAvailabilityQueue(company.getCompanySubdomain());
		}
	}
}