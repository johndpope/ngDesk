package com.ngdesk.websocket.channels.chat.dao;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ChatStatusMessage {

	@JsonProperty("COMPANY_ID")
	private String companyId;

	@JsonProperty("TYPE")
	private String type;

	@JsonProperty("CHAT_STATUS")
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
