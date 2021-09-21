package com.ngdesk.websocket.channels.chat.dao;

public class ChatStatusMessage {

	private String companyId;

	private String type;

	private String chatStatus;

	public ChatStatusMessage() {

	}

	public ChatStatusMessage(String companyId, String type, String chatStatus) {
		super();
		this.companyId = companyId;
		this.type = type;
		this.chatStatus = chatStatus;
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

}
