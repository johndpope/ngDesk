package com.ngdesk.websocket.channels.chat.dao;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import com.ngdesk.repositories.ChatChannelRepository;
import com.ngdesk.repositories.CompaniesRepository;
import com.ngdesk.repositories.ModuleEntryRepository;
import com.ngdesk.websocket.SessionService;
import com.ngdesk.websocket.UserSessions;
import com.ngdesk.websocket.companies.dao.Company;

@Component
public class AgentAvailabilityService {

	@Autowired
	SessionService sessionService;

	@Autowired
	ModuleEntryRepository entryRepository;

	@Autowired
	CompaniesRepository companiesRepository;

	@Autowired
	ChatChannelRepository chatChannelRepository;

	@Autowired
	RedisTemplate<String, AgentAvailability> redisTemplateForAgentAvailability;

	@Autowired
	RedisTemplate<String, ChatChannelMessage> redisTemplate;

	public void agentAvailability(AgentAvailability agentAvailability) {

		Optional<Company> optionalCompany = companiesRepository
				.findCompanyBySubdomain(agentAvailability.getCompanySubdomain());
		if (optionalCompany.isPresent()) {
			Company company = optionalCompany.get();
			String companyId = company.getId();

			ConcurrentHashMap<String, UserSessions> sessionMap = sessionService.sessions
					.get(company.getCompanySubdomain());

			String userId = null;
			Boolean isAgentAvailable = false;

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

						isAgentAvailable = true;

					}
				}

			}
			if (!isAgentAvailable) {
				AgentAvailability agentAvailable = new AgentAvailability(agentAvailability.getSessionUUID(),
						company.getCompanySubdomain(), "AgentAvailability", agentAvailability.getChannelName(), false);
				addToAgentAvailabilityQueue(agentAvailable);

			} else {
				AgentAvailability agentAvailable = new AgentAvailability(agentAvailability.getSessionUUID(),
						company.getCompanySubdomain(), "AgentAvailability", agentAvailability.getChannelName(), true);
				addToAgentAvailabilityQueue(agentAvailable);

			}

			Optional<ChatChannel> optionalChatChannel = chatChannelRepository
					.findChannelByName(agentAvailability.getChannelName(), "channels_chat_" + companyId);
			if (optionalChatChannel.isPresent()) {
				ChatChannelMessage chatChannelMessage = new ChatChannelMessage(companyId,
						agentAvailability.getSessionUUID(), optionalChatChannel.get(), "CHAT_CHANNEL");
				addToChatChannelQueue(chatChannelMessage);

			}

		}

	}

	public void addToAgentAvailabilityQueue(AgentAvailability message) {
		redisTemplateForAgentAvailability.convertAndSend("agentAvailability_notification", message);
	}

	public void addToChatChannelQueue(ChatChannelMessage chatChannelMessage) {
		redisTemplate.convertAndSend("chat_channel", chatChannelMessage);
	}

}
