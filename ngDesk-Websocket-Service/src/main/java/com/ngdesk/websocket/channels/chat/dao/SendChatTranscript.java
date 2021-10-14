
package com.ngdesk.websocket.channels.chat.dao;

public class SendChatTranscript {

	private String sessionUUID;

	private String subdomain;

	private Boolean closeSession;

	public SendChatTranscript() {

	}

	public SendChatTranscript(String sessionUUID, String subdomain, Boolean closeSession) {
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

	public Boolean getCloseSession() {
		return closeSession;
	}

	public void setCloseSession(Boolean closeSession) {
		this.closeSession = closeSession;
	}

}
