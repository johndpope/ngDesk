package com.ngdesk.websocket.channels.chat.dao;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import com.ngdesk.repositories.CompaniesRepository;
import com.ngdesk.repositories.ModuleEntryRepository;
import com.ngdesk.websocket.SessionService;
import com.ngdesk.websocket.UserSessions;
import com.ngdesk.websocket.companies.dao.Company;

@Component
public class FindAgentService {

	@Autowired
	SessionService sessionService;

	@Autowired
	ModuleEntryRepository entryRepository;

	@Autowired
	CompaniesRepository companiesRepository;

	@Autowired
	RedisTemplate<String, FindAgent> redisTemplateForFindAgent;

	public void findAgent(FindAgent findAgent) {

		Optional<Company> optionalCompany = companiesRepository.findCompanyBySubdomain(findAgent.getCompanySubdomain());
		if (optionalCompany.isPresent()) {
			Company company = optionalCompany.get();
			String companyId = company.getId();

			ConcurrentHashMap<String, UserSessions> sessionMap = sessionService.sessions
					.get(company.getCompanySubdomain());

			String userId = null;

			for (String key : sessionMap.keySet()) {
				userId = key;
				Optional<Map<String, Object>> optionalUserEntry = entryRepository.findById(userId,
						"Users_" + companyId);
				if (optionalUserEntry.isPresent()) {
					Integer chatEntries = entryRepository.findByAgentAndCollectionName(userId.toString(),
							"Chats_" + company.getId());
					UserSessions userSessions = sessionMap.get(key);
					String chatStatus = userSessions.getChatStatus();
					if (chatEntries < company.getChatSettings().getMaxChatPerAgent() && chatStatus != null
							&& chatStatus.equalsIgnoreCase("available")) {
						FindAgent findingAgent = new FindAgent(findAgent.getSessionUUID(),
								company.getCompanySubdomain(), "AgentAvailability", true);
						addToFindAgentQueue(findingAgent);

					} else {
						FindAgent findingAgent = new FindAgent(findAgent.getSessionUUID(),
								company.getCompanySubdomain(), "AgentAvailability", false);
						addToFindAgentQueue(findingAgent);

					}
				}

			}
		}

	}

	public void addToFindAgentQueue(FindAgent message) {
		redisTemplateForFindAgent.convertAndSend("findAgent_notification", message);
	}

}
