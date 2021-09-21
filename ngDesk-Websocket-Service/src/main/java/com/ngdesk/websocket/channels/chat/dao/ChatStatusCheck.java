package com.ngdesk.websocket.channels.chat.dao;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ChatStatusCheck {

	@JsonProperty("USER_ID")
	String userId;

	@JsonProperty("COMPANY_SUBDOMAIN")
	String subdomain;

	@JsonProperty("STATUS_CHECK")
	private boolean statusCheck;

	public ChatStatusCheck() {

	}

	public ChatStatusCheck(String userId, String subdomain, boolean statusCheck) {
		super();
		this.userId = userId;
		this.subdomain = subdomain;
		this.statusCheck = statusCheck;
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

	public boolean isStatusCheck() {
		return statusCheck;
	}

	public void setStatusCheck(boolean statusCheck) {
		this.statusCheck = statusCheck;
	}

}
