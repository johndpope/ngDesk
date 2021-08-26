package com.ngdesk.data.dao;

import java.util.Date;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

public class WorkflowPayload {

	@JsonProperty("USER_ID")
	private String userId;

	@JsonProperty("MODULE")
	private String moduleId;

	@JsonProperty("COMPANY_ID")
	private String companyId;

	@JsonProperty("DATA_ID")
	String dataId;

	@JsonProperty("OLD_COPY")
	private Map<String, Object> oldCopy;

	@JsonProperty("TYPE")
	private String requestType;

	@JsonProperty("DATE_CREATED")
	private Date dateCreated;

	public WorkflowPayload() {
	}

	public WorkflowPayload(String userId, String moduleId, String companyId, String dataId, Map<String, Object> oldCopy,
			String requestType, Date dateCreated) {
		super();
		this.userId = userId;
		this.moduleId = moduleId;
		this.companyId = companyId;
		this.dataId = dataId;
		this.oldCopy = oldCopy;
		this.requestType = requestType;
		this.dateCreated = dateCreated;
	}

	public String getModuleId() {
		return moduleId;
	}

	public void setModuleId(String moduleId) {
		this.moduleId = moduleId;
	}

	public String getCompanyId() {
		return companyId;
	}

	public void setCompanyId(String companyId) {
		this.companyId = companyId;
	}

	public String getDataId() {
		return dataId;
	}

	public void setDataId(String dataId) {
		this.dataId = dataId;
	}

	public Map<String, Object> getOldCopy() {
		return oldCopy;
	}

	public void setOldCopy(Map<String, Object> oldCopy) {
		this.oldCopy = oldCopy;
	}

	public String getRequestType() {
		return requestType;
	}

	public void setRequestType(String requestType) {
		this.requestType = requestType;
	}

	public Date getDateCreated() {
		return dateCreated;
	}

	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

}
