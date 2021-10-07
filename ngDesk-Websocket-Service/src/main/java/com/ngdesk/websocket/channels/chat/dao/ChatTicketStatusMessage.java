package com.ngdesk.websocket.channels.chat.dao;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ChatTicketStatusMessage {

	@JsonProperty("COMPANY_ID")
	private String companyId;

	@JsonProperty("SESSION_UUID")
	private String sessionUUId;

	@JsonProperty("MESSAGE_TYPE")
	private String messageType;

	@JsonProperty("STATUS")
	private String status;

	@JsonProperty("MESSAGE")
	private String message;

	public ChatTicketStatusMessage() {

	}

	public ChatTicketStatusMessage(String companyId, String sessionUUId, String messageType, String status,
			String message) {
		super();
		this.companyId = companyId;
		this.sessionUUId = sessionUUId;
		this.messageType = messageType;
		this.status = status;
		this.message = message;
	}

	public String getCompanyId() {
		return companyId;
	}

	public void setCompanyId(String companyId) {
		this.companyId = companyId;
	}

	public String getMessageType() {
		return messageType;
	}

	public void setMessageType(String messageType) {
		this.messageType = messageType;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getSessionUUId() {
		return sessionUUId;
	}

	public void setSessionUUId(String sessionUUId) {
		this.sessionUUId = sessionUUId;
	}

}
