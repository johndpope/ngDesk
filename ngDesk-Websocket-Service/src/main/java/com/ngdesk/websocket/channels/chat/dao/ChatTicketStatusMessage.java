package com.ngdesk.websocket.channels.chat.dao;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ChatTicketStatusMessage {

	@JsonProperty("COMPANY_ID")
	private String companyId;

	@JsonProperty("SESSION_UUID")
	private String sessionUUId;

	@JsonProperty("TYPE")
	private String type;

	@JsonProperty("STATUS")
	private String status;

	@JsonProperty("MESSAGE")
	private String message;

	public ChatTicketStatusMessage() {

	}

	public ChatTicketStatusMessage(String companyId, String sessionUUId, String type, String status, String message) {
		super();
		this.companyId = companyId;
		this.sessionUUId = sessionUUId;
		this.type = type;
		this.status = status;
		this.message = message;
	}

	public String getCompanyId() {
		return companyId;
	}

	public void setCompanyId(String companyId) {
		this.companyId = companyId;
	}

	public String getSessionUUId() {
		return sessionUUId;
	}

	public void setSessionUUId(String sessionUUId) {
		this.sessionUUId = sessionUUId;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
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

}