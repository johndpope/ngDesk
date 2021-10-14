package com.ngdesk.websocket.notification.dao;

import java.util.Date;

public class AgentDetails {

	private String companyId;

	private String agentFirstName;

	private String agentLastName;

	private String agentDataId;

	private String customerDataId;

	private Boolean agentsAvailable;

	private String sessionUuid;

	private Date agentAssignedTime;

	private String agentRoleId;

	private String customerRoleId;

	private String customerUuid;

	private String chatEntryId;

	public AgentDetails() {

	}

	public AgentDetails(String companyId, String agentFirstName, String agentLastName, String agentDataId,
			String customerDataId, Boolean agentsAvailable, String sessionUuid, Date agentAssignedTime,
			String agentRoleId, String customerRoleId, String customerUuid, String chatEntryId) {
		super();
		this.companyId = companyId;
		this.agentFirstName = agentFirstName;
		this.agentLastName = agentLastName;
		this.agentDataId = agentDataId;
		this.customerDataId = customerDataId;
		this.agentsAvailable = agentsAvailable;
		this.sessionUuid = sessionUuid;
		this.agentAssignedTime = agentAssignedTime;
		this.agentRoleId = agentRoleId;
		this.customerRoleId = customerRoleId;
		this.customerUuid = customerUuid;
		this.chatEntryId = chatEntryId;
	}

	public String getCompanyId() {
		return companyId;
	}

	public void setCompanyId(String companyId) {
		this.companyId = companyId;
	}

	public String getAgentFirstName() {
		return agentFirstName;
	}

	public void setAgentFirstName(String agentFirstName) {
		this.agentFirstName = agentFirstName;
	}

	public String getAgentLastName() {
		return agentLastName;
	}

	public void setAgentLastName(String agentLastName) {
		this.agentLastName = agentLastName;
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

	public Date getAgentAssignedTime() {
		return agentAssignedTime;
	}

	public void setAgentAssignedTime(Date agentAssignedTime) {
		this.agentAssignedTime = agentAssignedTime;
	}

	public String getAgentRoleId() {
		return agentRoleId;
	}

	public void setAgentRoleId(String agentRoleId) {
		this.agentRoleId = agentRoleId;
	}

	public String getCustomerRoleId() {
		return customerRoleId;
	}

	public void setCustomerRoleId(String customerRoleId) {
		this.customerRoleId = customerRoleId;
	}

	public String getCustomerUuid() {
		return customerUuid;
	}

	public void setCustomerUuid(String customerUuid) {
		this.customerUuid = customerUuid;
	}

	public String getChatEntryId() {
		return chatEntryId;
	}

	public void setChatEntryId(String chatEntryId) {
		this.chatEntryId = chatEntryId;
	}

}
