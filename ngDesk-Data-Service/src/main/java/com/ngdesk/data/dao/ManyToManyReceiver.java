package com.ngdesk.data.dao;

import java.util.Map;

public class ManyToManyReceiver {

	private String moduleId;

	private String entryId;

	private String companyId;

	private Map<String, Object> entry;

	private Map<String, Object> existingEntry;

	public ManyToManyReceiver() {

	}

	public ManyToManyReceiver(String moduleId, String entryId, String companyId, Map<String, Object> entry,
			Map<String, Object> existingEntry) {
		super();
		this.moduleId = moduleId;
		this.entryId = entryId;
		this.companyId = companyId;
		this.entry = entry;
		this.existingEntry = existingEntry;
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

	public String getCompanyId() {
		return companyId;
	}

	public void setCompanyId(String companyId) {
		this.companyId = companyId;
	}

	public Map<String, Object> getEntry() {
		return entry;
	}

	public void setEntry(Map<String, Object> entry) {
		this.entry = entry;
	}

	public Map<String, Object> getExistingEntry() {
		return existingEntry;
	}

	public void setExistingEntry(Map<String, Object> existingEntry) {
		this.existingEntry = existingEntry;
	}

}
