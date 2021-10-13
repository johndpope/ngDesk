package com.ngdesk.websocket.notification.dao;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AgentDetails {

	@JsonProperty("COMPANY_ID")
	private String companyId;

	@JsonProperty("AGENT_FIRST_NAME")
	private String agentFirstName;

	@JsonProperty("AGENT_LAST_NAME")
	private String agentLastName;

	@JsonProperty("AGENT_DATA_ID")
	private String agentDataId;

	@JsonProperty("CUSTOMER_DATA_ID")
	private String customerDataId;

	@JsonProperty("AGENTS_AVAILABLE")
	private Boolean agentsAvailable;

	@JsonProperty("SESSION_UUID")
	private String sessionUuid;

	@JsonProperty("AGENT_ASSIGNED_TIME")
	private Date agentAssignedTime;

	@JsonProperty("AGENT_ROLE_ID")
	private String agentRoleId;

	@JsonProperty("CUSTOMER_ROLE_ID")
	private String customerRoleId;

	@JsonProperty("CUSTOMER_UUID")
	private String customerUuid;

	@JsonProperty("CHAT_ENTRY_ID")
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
