package com.ngdesk.data.elastic;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ElasticMessage {
	
	@JsonProperty("MODULE_ID")
	private String moduleId;
	
	@JsonProperty("COMPANY_ID")
	private String companyId;
	
	@JsonProperty("ENTRY")
	private Map<String, Object> entry;
	
	public ElasticMessage() {
		
	}

	public ElasticMessage(String moduleId, String companyId, Map<String, Object> entry) {
		super();
		this.moduleId = moduleId;
		this.companyId = companyId;
		this.entry = entry;
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

	public Map<String, Object> getEntry() {
		return entry;
	}

	public void setEntry(Map<String, Object> entry) {
		this.entry = entry;
	}
	
	
}
