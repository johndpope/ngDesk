package com.ngdesk.websocket.channels.chat.dao;

public class ChatStatus {

	private String userId;

	private String subdomain;

	private boolean accepting;

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