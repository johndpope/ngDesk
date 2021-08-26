package com.ngdesk.data.dao;

import java.util.Map;

public class MetadataPayload {

	private String companyId;

	private String moduleId;

	private String entryId;

	private String userId;

	private Map<String, Object> existingEntry;

	private Map<String, Object> entry;

	public MetadataPayload() {

	}

	public MetadataPayload(String companyId, String moduleId, String entryId, String userId,
			Map<String, Object> existingEntry, Map<String, Object> entry) {
		super();
		this.companyId = companyId;
		this.moduleId = moduleId;
		this.entryId = entryId;
		this.userId = userId;
		this.existingEntry = existingEntry;
		this.entry = entry;
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

	public String getEntryId() {
		return entryId;
	}

	public void setEntryId(String entryId) {
		this.entryId = entryId;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public Map<String, Object> getExistingEntry() {
		return existingEntry;
	}

	public void setExistingEntry(Map<String, Object> existingEntry) {
		this.existingEntry = existingEntry;
	}

	public Map<String, Object> getEntry() {
		return entry;
	}

	public void setEntry(Map<String, Object> entry) {
		this.entry = entry;
	}

}
