
package com.ngdesk.websocket.channels.chat.dao;

public class SendChatTranscript {

	private String sessionUUID;

	private String subdomain;

	private Boolean sendTranscript;

	public SendChatTranscript() {

	}

	public SendChatTranscript(String sessionUUID, String subdomain, Boolean sendTranscript) {
		super();
		this.sessionUUID = sessionUUID;
		this.subdomain = subdomain;
		this.sendTranscript = sendTranscript;
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

	public Boolean getSendTranscript() {
		return sendTranscript;
	}

	public void setSendTranscript(Boolean sendTranscript) {
		this.sendTranscript = sendTranscript;
	}

}
