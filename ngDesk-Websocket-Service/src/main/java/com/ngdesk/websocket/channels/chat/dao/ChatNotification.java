package com.ngdesk.websocket.channels.chat.dao;

import java.util.Map;

import com.ngdesk.websocket.notification.dao.AgentDetails;

public class ChatNotification {

	private String companyId;

	private String type;

	private String sessionUUID;

	private Map<String, Object> entry;

	private String status;

	private AgentDetails agentDetails;

	public ChatNotification() {
		super();
	}

	public ChatNotification(String companyId, String type, String sessionUUID, Map<String, Object> entry, String status,
			AgentDetails agentDetails) {
		super();
		this.companyId = companyId;
		this.type = type;
		this.sessionUUID = sessionUUID;
		this.entry = entry;
		this.status = status;
		this.agentDetails = agentDetails;
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

	public String getSessionUUID() {
		return sessionUUID;
	}

	public void setSessionUUID(String sessionUUID) {
		this.sessionUUID = sessionUUID;
	}

	public Map<String, Object> getEntry() {
		return entry;
	}

	public void setEntry(Map<String, Object> entry) {
		this.entry = entry;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public AgentDetails getAgentDetails() {
		return agentDetails;
	}

	public void setAgentDetails(AgentDetails agentDetails) {
		this.agentDetails = agentDetails;
	}

}
