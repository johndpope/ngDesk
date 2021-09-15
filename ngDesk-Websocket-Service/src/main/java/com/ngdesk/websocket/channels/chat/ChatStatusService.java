package com.ngdesk.websocket.channels.chat;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ngdesk.repositories.CompaniesRepository;
import com.ngdesk.websocket.UserSessions;
import com.ngdesk.websocket.SessionService;
import com.ngdesk.websocket.companies.dao.Company;

@Component
public class ChatStatusService {

	@Autowired
	SessionService sessionService;

	@Autowired
	CompaniesRepository companiesRepository;

	public void updateChatStatus(ChatStatus chatStatus) {
		System.out.println(chatStatus.toString());
		System.out.println(sessionService.sessions);
		if (chatStatus.getSubdomain() != null) {
			Optional<Company> optionalComapny = companiesRepository.findCompanyBySubdomain(chatStatus.getSubdomain());
			if (optionalComapny.isPresent()) {
				Company company = optionalComapny.get();
				ConcurrentHashMap<String, UserSessions> sessionMap = sessionService.sessions
						.get(company.getCompanySubdomain());
				UserSessions userSessions = sessionMap.get(chatStatus.getUserId());
				if (chatStatus.isAccepting()) {
					userSessions.setChatStatus("available");
				} else {
					userSessions.setChatStatus("not available");

				}
				sessionMap.put(chatStatus.getUserId(), userSessions);
				sessionService.sessions.put(chatStatus.getSubdomain(), sessionMap);
				System.out.println(sessionService.sessions);
			}
		}

	}

}
