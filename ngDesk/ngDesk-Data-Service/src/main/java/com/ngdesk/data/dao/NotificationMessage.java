package com.ngdesk.data.dao;

import com.fasterxml.jackson.annotation.JsonProperty;

public class NotificationMessage {

	@JsonProperty("MODULE_ID")
	private String moduleId;

	@JsonProperty("COMPANY_ID")
	private String companyId;

	@JsonProperty("TYPE")
	private String type;

	@JsonProperty("DATA_ID")
	private String dataId;

	public NotificationMessage() {

	}

	public NotificationMessage(String moduleId, String companyId, String type, String dataId) {
		super();
		this.moduleId = moduleId;
		this.companyId = companyId;
		this.type = type;
		this.dataId = dataId;
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

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getDataId() {
		return dataId;
	}

	public void setDataId(String dataId) {
		this.dataId = dataId;
	}

}
