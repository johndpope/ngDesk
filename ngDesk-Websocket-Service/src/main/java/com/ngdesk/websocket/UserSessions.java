package com.ngdesk.websocket;

import java.util.concurrent.ConcurrentLinkedQueue;

import org.springframework.web.socket.WebSocketSession;

public class UserSessions {

	private ConcurrentLinkedQueue<WebSocketSession> sessions;

	private String chatStatus;

	public UserSessions() {

	}

	public UserSessions(ConcurrentLinkedQueue<WebSocketSession> sessions, String chatStatus) {
		super();
		this.sessions = sessions;
		this.chatStatus = chatStatus;
	}

	public ConcurrentLinkedQueue<WebSocketSession> getSessions() {
		return sessions;
	}

	public void setSessions(ConcurrentLinkedQueue<WebSocketSession> sessions) {
		this.sessions = sessions;
	}

	public String getChatStatus() {
		return chatStatus;
	}

	public void setChatStatus(String chatStatus) {
		this.chatStatus = chatStatus;
	}

	@Override
	public String toString() {
		return "UserSessions [sessions=" + sessions + ", chatStatus=" + chatStatus + "]";
	}

}
