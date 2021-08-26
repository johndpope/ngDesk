package com.ngdesk.data.dao;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SingleWorkflowPayload {

	@JsonProperty("COMPANY_ID")
	private String companyId;

	@JsonProperty("MODULE_ID")
	private String moduleId;

	@JsonProperty("DATA_ID")
	private String dataId;

	@JsonProperty("WORKFLOW_ID")
	private String workflowId;

	@JsonProperty("USER_ID")
	private String userId;

	@JsonProperty("DATE_CREATED")
	private Date dateCreated;

	public SingleWorkflowPayload() {

	}

	public SingleWorkflowPayload(String companyId, String moduleId, String dataId, String workflowId, String userId,
			Date dateCreated) {
		this.companyId = companyId;
		this.moduleId = moduleId;
		this.dataId = dataId;
		this.workflowId = workflowId;
		this.userId = userId;
		this.dateCreated = dateCreated;
	}

	public String getCompanyId() {
		return companyId;
	}

	public void setCompanyId(String companyId) {
		this.companyId = companyId;
	}

	public String getModuleId() {
		return moduleId;
	}

	public void setModuleId(String moduleId) {
		this.moduleId = moduleId;
	}

	public String getDataId() {
		return dataId;
	}

	public void setDataId(String dataId) {
		this.dataId = dataId;
	}

	public String getWorkflowId() {
		return workflowId;
	}

	public void setWorkflowId(String workflowId) {
		this.workflowId = workflowId;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public Date getDateCreated() {
		return dateCreated;
	}

	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}

}
