package com.ngdesk.websocket.channels.chat.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ngdesk.websocket.SessionService;

@Component
public class CloseChatSessionService {

	@Autowired
	SessionService sessionService;

	public void closeChatSession(ChatSession chatSession) {
		if (chatSession.isCloseSession()) {
			sessionService.sessions.get(chatSession.getSubdomain()).remove(chatSession.getSessionUUID());
		}
	}
}
