package com.ngdesk.websocket.sam.dao;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ngdesk.repositories.CompaniesRepository;
import com.ngdesk.repositories.SamFileRuleRepository;
import com.ngdesk.websocket.SessionService;

@Component
public class FileRuleService {
	@Autowired
	SessionService sessionService;

	@Autowired
	SamFileRuleRepository fileRuleRepository;

	@Autowired
	CompaniesRepository companiesRepository;

	public void publishRuleToProbe(String subdomain, String controllerId, FileRuleNotification fileRule) {

		Optional<SamFileRule> optionalFileRule = fileRuleRepository.findById(fileRule.getRuleId());
		if (optionalFileRule.isEmpty()) {
			return;
		}
		SamFileRule samFileRule = optionalFileRule.get();

		try {
			System.out.println(new ObjectMapper().writeValueAsString(fileRule));

			if (sessionService.probeSessions.containsKey(subdomain)) {

				ConcurrentHashMap<String, ConcurrentHashMap<String, WebSocketSession>> companyControllers = sessionService.probeSessions
						.get(subdomain);
				if (companyControllers.containsKey(controllerId)) {

					ConcurrentHashMap<String, WebSocketSession> probeSessions = companyControllers.get(controllerId);

					if (probeSessions.containsKey(getProbeTypeFromName("ngDesk-Software-Probe"))) {

						WebSocketSession session = probeSessions.get(getProbeTypeFromName("ngDesk-Software-Probe"));
						String payload = new ObjectMapper().writeValueAsString(samFileRule);

						System.out.println();
						session.sendMessage(new TextMessage(payload));
					}
				}
			}
		} catch (

		Exception e) {
			e.printStackTrace();
		}
	}

	private String getProbeTypeFromName(String applicationName) {
		switch (applicationName) {
		case "ngDesk-Controller":
			return "CONTROLLER";
		case "ngDesk-Asset-Probe":
			return "ASSET";
		case "ngDesk-Software-Probe":
			return "SOFTWARE";
		}

		return null;
	}
}
