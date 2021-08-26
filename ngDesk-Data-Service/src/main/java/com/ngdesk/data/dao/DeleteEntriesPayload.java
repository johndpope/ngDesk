package com.ngdesk.data.dao;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DeleteEntriesPayload {

	@JsonProperty("COMPANY_ID")
	private String companyId;

	@JsonProperty("MODULE_ID")
	private String moduleId;

	@JsonProperty("ENTRY_ID")
	private List<String> entryIds;

	@JsonProperty("USER_ID")
	private String userId;

	public DeleteEntriesPayload() {
	}

	public DeleteEntriesPayload(String companyId, String moduleId, List<String> entryIds, String userId) {
		super();
		this.companyId = companyId;
		this.moduleId = moduleId;
		this.entryIds = entryIds;
		this.userId = userId;
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

	public List<String> getEntryIds() {
		return entryIds;
	}

	public void setEntryIds(List<String> entryIds) {
		this.entryIds = entryIds;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

}
