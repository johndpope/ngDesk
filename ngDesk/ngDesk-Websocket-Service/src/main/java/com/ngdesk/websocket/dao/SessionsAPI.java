package com.ngdesk.websocket.dao;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ngdesk.commons.exceptions.BadRequestException;
import com.ngdesk.commons.exceptions.UnauthorizedException;
import com.ngdesk.websocket.SessionService;
import com.ngdesk.websocket.sam.dao.Instruction;

@RestController
@RefreshScope
public class SessionsAPI {
	
	private final Logger log = LoggerFactory.getLogger(SessionsAPI.class);

	@Autowired
	SessionService sessionService;

	@GetMapping("/company/sessions")
	public Map<String, List<Boolean>> getCompanySession(@RequestParam("access_key") String accessKey,
			@RequestParam("subdomain") String subdomain) {

		if (!accessKey.equals("f48bcde8-ed7b-4632-b5ad-3f0c5421b875")) {
			throw new UnauthorizedException("UNAUTHORIZED");
		}

		if (!sessionService.sessions.containsKey(subdomain)) {
			throw new BadRequestException("SUBDOMAIN_MISSING", null);
		}

		ConcurrentHashMap<String, ConcurrentLinkedQueue<WebSocketSession>> userSessions = sessionService.sessions
				.get(subdomain);

		if (userSessions == null) {
			throw new BadRequestException("SESSIONS_NULL", null);
		}
		
		Map<String, List<Boolean>> responseMap = new HashMap<String, List<Boolean>>();
		
		
		for (String key: userSessions.keySet()) {
			List<Boolean> sessionsOpen = new ArrayList<Boolean>();
			ConcurrentLinkedQueue<WebSocketSession> sessions = userSessions.get(key);
			for (WebSocketSession session: sessions) {
				sessionsOpen.add(session.isOpen());
			}
			responseMap.put(key, sessionsOpen);
		}
		
		return responseMap;
	}

	@GetMapping("/company/probes/sessions")
	public Set getCompanyProbes(@RequestParam("access_key") String accessKey,
			@RequestParam("subdomain") String subdomain, @RequestParam("type") String type) {
		
		if (!accessKey.equals("f48bcde8-ed7b-4632-b5ad-3f0c5421b875")) {
			throw new UnauthorizedException("UNAUTHORIZED");
		}
		
		if (!sessionService.probeSessions.containsKey(subdomain)) {
			throw new BadRequestException("SUBDOMAIN_MISSING", null);
		}
		
		ConcurrentHashMap<String, ConcurrentHashMap<String, WebSocketSession>> companyProbes = sessionService.probeSessions.get(subdomain);
		ConcurrentHashMap<String, WebSocketSession> probeSessions = companyProbes.get(type);
		
		try {
			for (String key: probeSessions.keySet()) {
				WebSocketSession session = probeSessions.get(key);
				Instruction instruction = new Instruction("ngDesk-Controller", "UPDATE", "INFO",new HashMap<String,Object>());
				System.out.println("gets here");
				String payload = new ObjectMapper().writeValueAsString(instruction);
				session.sendMessage(new TextMessage(payload));
			}
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return probeSessions.keySet();
	}

}
