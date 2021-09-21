package com.ngdesk.websocket.channels.chat.dao;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import com.ngdesk.repositories.CompaniesRepository;
import com.ngdesk.websocket.SessionService;
import com.ngdesk.websocket.UserSessions;
import com.ngdesk.websocket.companies.dao.Company;

@Component
public class ChatStatusService {

	@Autowired
	SessionService sessionService;

	@Autowired
	CompaniesRepository companiesRepository;

	@Autowired
	RedisTemplate<String, ChatStatusMessage> redisTemplate;

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
					userSessions.setChatStatus("available");
					chatStatusMessage.setChatStatus("available");
					chatStatusMessage.setCompanyId(company.getId());
					chatStatusMessage.setType("CHAT_STATUS");
					sessionMap.put(chatStatus.getUserId(), userSessions);
					sessionService.sessions.put(company.getCompanySubdomain(), sessionMap);

				} else {
					userSessions.setChatStatus("not available");
					chatStatusMessage.setChatStatus("not available");
					chatStatusMessage.setCompanyId(company.getId());
					chatStatusMessage.setType("CHAT_STATUS");
					sessionMap.put(chatStatus.getUserId(), userSessions);
					sessionService.sessions.put(company.getCompanySubdomain(), sessionMap);

				}
				sessionMap.put(chatStatus.getUserId(), userSessions);
				sessionService.sessions.put(chatStatus.getSubdomain(), sessionMap);
				addToQueue(chatStatusMessage);
			}
		}

	}

	public void addToQueue(ChatStatusMessage notification) {
		redisTemplate.convertAndSend("chat_status", notification);
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
				addToQueue(chatStatusMessage);
			}
		}

	}

}
