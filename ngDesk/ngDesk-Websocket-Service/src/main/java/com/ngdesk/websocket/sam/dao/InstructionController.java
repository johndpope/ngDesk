package com.ngdesk.websocket.sam.dao;

import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ngdesk.websocket.SessionService;

@Component
public class InstructionController {

	@Autowired
	SessionService sessionService;

	public void publishInstructionToProbe(String subdomain, String controllerId, Instruction instruction) {
		try {

			System.out.println(new ObjectMapper().writeValueAsString(instruction));

			if (sessionService.probeSessions.containsKey(subdomain)) {

				ConcurrentHashMap<String, ConcurrentHashMap<String, WebSocketSession>> companyControllers = sessionService.probeSessions
						.get(subdomain);

				if (companyControllers.containsKey(controllerId)) {

					ConcurrentHashMap<String, WebSocketSession> probeSessions = companyControllers.get(controllerId);

					if (probeSessions.containsKey(getProbeTypeFromName("ngDesk-Controller"))
							&& (!instruction.getAction().equals("LOG_UPDATE"))) {
						WebSocketSession session = probeSessions.get(getProbeTypeFromName("ngDesk-Controller"));
						String payload = new ObjectMapper().writeValueAsString(instruction);
						System.out.println();
						session.sendMessage(new TextMessage(payload));
					} else {
						if (probeSessions.containsKey(getProbeTypeFromName(instruction.getApplicationName()))) {
							WebSocketSession session = probeSessions
									.get(getProbeTypeFromName(instruction.getApplicationName()));
							String payload = new ObjectMapper().writeValueAsString(instruction);
							System.out.println();
							session.sendMessage(new TextMessage(payload));
						}
					}
				}
			}

		} catch (Exception e) {
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
		case "ngDesk-Patch-Probe":
			return "PATCH";
		}

		return null;
	}
}
