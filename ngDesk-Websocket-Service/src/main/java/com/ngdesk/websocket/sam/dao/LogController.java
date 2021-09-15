package com.ngdesk.websocket.sam.dao;

import java.io.IOException;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ngdesk.repositories.CompaniesRepository;
import com.ngdesk.repositories.LogsRepository;
import com.ngdesk.websocket.UserSessions;
import com.ngdesk.websocket.SessionService;
import com.ngdesk.websocket.companies.dao.Company;

@Component
public class LogController {

	@Autowired
	private CompaniesRepository companiesRepository;

	@Autowired
	private LogsRepository logsRepository;

	@Autowired
	private SessionService sessionService;

	public void addLogToApplication(Log log, String subdomain, String id) {

		Optional<Company> optionalCompany = companiesRepository.findCompanyBySubdomain(subdomain);
		if (optionalCompany.isPresent()) {
			Company company = optionalCompany.get();

			log.setControllerId(id);
			log.setCompanyId(company.getId());
			log.setDateCreated(new Date());

			logsRepository.save(log, "probe_logs");
			publishLogToUsersInvolved(company, log);
		}

	}

	public void publishLogToUsersInvolved(Company company, Log log) {
		ObjectMapper mapper = new ObjectMapper();
		log.setType("PROBE_LOG");
		if (!sessionService.sessions.containsKey(company.getCompanySubdomain())) {
			return;
		}
		ConcurrentHashMap<String, UserSessions> sessions = sessionService.sessions.get(company.getCompanySubdomain());
		for (String userId : sessions.keySet()) {
			ConcurrentLinkedQueue<WebSocketSession> userSessions = sessions.get(userId).getSessions();
			userSessions.forEach(session -> {
				try {
					String payload = mapper.writeValueAsString(log);
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
