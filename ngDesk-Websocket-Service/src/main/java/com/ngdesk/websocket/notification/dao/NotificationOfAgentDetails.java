package com.ngdesk.websocket.notification.dao;

import java.util.Date;

public class NotificationOfAgentDetails {

	private String companyId;

	private String agentName;

	private String agentDataId;

	private String customerDataId;

	private Boolean agentsAvailable;

	private String sessionUuid;

	private String messageType;

	private Date agentAssignedTime;

	public NotificationOfAgentDetails() {

	}

	public NotificationOfAgentDetails(String companyId, String agentName, String agentDataId, String customerDataId,
			Boolean agentsAvailable, String sessionUuid, String messageType, Date agentAssignedTime) {
		super();
		this.companyId = companyId;
		this.agentName = agentName;
		this.agentDataId = agentDataId;
		this.customerDataId = customerDataId;
		this.agentsAvailable = agentsAvailable;
		this.sessionUuid = sessionUuid;
		this.messageType = messageType;
		this.agentAssignedTime = agentAssignedTime;
	}

	public String getCompanyId() {
		return companyId;
	}

	public void setCompanyId(String companyId) {
		this.companyId = companyId;
	}

	public String getAgentName() {
		return agentName;
	}

	public void setAgentName(String agentName) {
		this.agentName = agentName;
	}

	public String getAgentDataId() {
		return agentDataId;
	}

	public void setAgentDataId(String agentDataId) {
		this.agentDataId = agentDataId;
	}

	public String getCustomerDataId() {
		return customerDataId;
	}

	public void setCustomerDataId(String customerDataId) {
		this.customerDataId = customerDataId;
	}

	public Boolean getAgentsAvailable() {
		return agentsAvailable;
	}

	public void setAgentsAvailable(Boolean agentsAvailable) {
		this.agentsAvailable = agentsAvailable;
	}

	public String getSessionUuid() {
		return sessionUuid;
	}

	public void setSessionUuid(String sessionUuid) {
		this.sessionUuid = sessionUuid;
	}

	public String getMessageType() {
		return messageType;
	}

	public void setMessageType(String messageType) {
		this.messageType = messageType;
	}

	public Date getAgentAssignedTime() {
		return agentAssignedTime;
	}

	public void setAgentAssignedTime(Date agentAssignedTime) {
		this.agentAssignedTime = agentAssignedTime;
	}

}
