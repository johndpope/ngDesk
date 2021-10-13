
package com.ngdesk.websocket.channels.chat.dao;

import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SendChatTranscript {

	@JsonProperty("SESSION_UUID")
	@Field("SESSION_UUID")
	private String sessionUUID;

	@JsonProperty("SUBDOMAIN")
	@Field("SUBDOMAIN")
	private String subdomain;

	public SendChatTranscript() {

	}

	public SendChatTranscript(String sessionUUID, String subdomain) {
		super();
		this.sessionUUID = sessionUUID;
		this.subdomain = subdomain;
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

}
