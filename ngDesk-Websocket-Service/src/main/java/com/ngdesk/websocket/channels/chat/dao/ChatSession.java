package com.ngdesk.websocket.channels.chat.dao;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ChatSession {

	@JsonProperty("SESSION_UUID")
	private String sessionUUID;

	@JsonProperty("COMPANY_SUBDOMAIN")
	private String subdomain;

	@JsonProperty("CLOSE_SESSION")
	private boolean closeSession;

	public ChatSession() {
		super();
	}

	public ChatSession(String sessionUUID, String subdomain, boolean closeSession) {
		super();
		this.sessionUUID = sessionUUID;
		this.subdomain = subdomain;
		this.closeSession = closeSession;
	}

	public String getSessionUUID() {
		return sessionUUID;
	}

	public void setSessionUUID(String sessionUUID) {
		this.sessionUUID = sessionUUID;
	}

	public String getSubdomain() {
		return subdomain;
	}

	public void setSubdomain(String subdomain) {
		this.subdomain = subdomain;
	}

	public boolean isCloseSession() {
		return closeSession;
	}

	public void setCloseSession(boolean closeSession) {
		this.closeSession = closeSession;
	}

}
