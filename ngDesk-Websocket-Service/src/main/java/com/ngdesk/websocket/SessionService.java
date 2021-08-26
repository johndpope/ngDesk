package com.ngdesk.websocket;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

@Component
public class SessionService {
	
	/*
	 * LEVEL 1: KEY => COMPANY_SUBDOMAIN, VALUE => MAP
	 * LEVEL 2: KEY => USER_ID, VALUE => LIST OF SESSIONS OF USER
	 */
	public ConcurrentHashMap<String, ConcurrentHashMap<String, ConcurrentLinkedQueue<WebSocketSession>>> sessions = new ConcurrentHashMap<String, ConcurrentHashMap<String, ConcurrentLinkedQueue<WebSocketSession>>>();
	
	/*
	 * LEVEL 1: KEY => COMPANY_SUBDOMAIN, VALUE => MAP
	 * LEVEL 2: KEY => CONTROLLER ID, VALUE => MAP
	 * LEVEL 3: KEY => PROBE TYPE ex: ASSET, SOFTWARE, PATCH etc.. VALUE => WebsocketSession
	 */
	public ConcurrentHashMap<String, ConcurrentHashMap<String, ConcurrentHashMap<String, WebSocketSession>>> probeSessions = new ConcurrentHashMap<String, ConcurrentHashMap<String, ConcurrentHashMap<String, WebSocketSession>>>();
	
}
