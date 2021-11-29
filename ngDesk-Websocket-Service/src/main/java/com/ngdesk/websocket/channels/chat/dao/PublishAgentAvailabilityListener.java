package com.ngdesk.websocket.channels.chat.dao;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ngdesk.repositories.CompaniesRepository;
import com.ngdesk.repositories.ModuleEntryRepository;
import com.ngdesk.websocket.SessionService;
import com.ngdesk.websocket.UserSessions;
import com.ngdesk.websocket.companies.dao.Company;
import com.ngdesk.websocket.dao.WebSocketService;

@Component
@RabbitListener(queues = "publish-agent-availability", concurrency = "5")
public class PublishAgentAvailabilityListener {

	@Autowired
	WebSocketService webSocketService;

	@Autowired
	CompaniesRepository companiesRepository;

	@Autowired
	SessionService sessionService;

	@Autowired
	ModuleEntryRepository moduleEntryRepository;

	@RabbitHandler
	public void onMessage(String subdomain) {

		Optional<Company> optionalCompany = companiesRepository.findCompanyBySubdomain(subdomain);
		if (optionalCompany.isPresent()) {
			Company company = optionalCompany.get();
			String companyId = company.getId();
			ConcurrentHashMap<String, UserSessions> sessionMap = sessionService.sessions.get(subdomain);
			for (String key : sessionMap.keySet()) {
				Optional<Map<String, Object>> optionalChatEntry = moduleEntryRepository.findBySessionUuid(key,
						"Chats_" + companyId);
				Boolean isChatting = false;
				if (optionalChatEntry.isPresent()) {
					String status = optionalChatEntry.get().get("STATUS").toString();
					if (status.equals("Chatting")) {
						isChatting = true;
					}
				}
				if (!isChatting) {
					UserSessions userSessions = sessionMap.get(key);
					if (!userSessions.getSessions().isEmpty()) {
						String queryParams = userSessions.getSessions().element().getUri().getQuery();
						if (queryParams.contains("sessionUUID")) {
							AgentAvailability agentAvailable = new AgentAvailability(key, subdomain,
									"AgentAvailability", "Chats", true, company.getChatSettings().getHasRestrictions(),
									"Agent is available");
							webSocketService.publishAgentAvailabilityNotification(optionalCompany.get(),
									agentAvailable);
						}
					}
				}
			}
		}

	}

}