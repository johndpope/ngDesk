package com.ngdesk.websocket.channels.chat.dao;

public class ChatStatusMessage {

	private String companyId;

	private String type;

	private String chatStatus;

	private String userId;

	public ChatStatusMessage() {

	}

	public ChatStatusMessage(String companyId, String type, String chatStatus, String userId) {
		super();
		this.companyId = companyId;
		this.type = type;
		this.chatStatus = chatStatus;
		this.userId = userId;
	}

	public String getCompanyId() {
		return companyId;
	}

	public void setCompanyId(String companyId) {
		this.companyId = companyId;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getChatStatus() {
		return chatStatus;
	}

	public void setChatStatus(String chatStatus) {
		this.chatStatus = chatStatus;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

}
