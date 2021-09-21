package com.ngdesk.websocket.channels.chat.dao;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class ChatStatus {

	@JsonProperty("USER_ID")
	String userId;

	@JsonProperty("COMPANY_SUBDOMAIN")
	String subdomain;

	@JsonProperty("ACCEPTING_CHATS")
	boolean accepting;

	public ChatStatus() {

	}

	public ChatStatus(String userId, String subdomain, boolean accepting) {
		super();
		this.userId = userId;
		this.subdomain = subdomain;
		this.accepting = accepting;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getSubdomain() {
		return subdomain;
	}

	public void setSubdomain(String subdomain) {
		this.subdomain = subdomain;
	}

	public boolean isAccepting() {
		return accepting;
	}

	public void setAccepting(boolean accepting) {
		this.accepting = accepting;
	}

}
