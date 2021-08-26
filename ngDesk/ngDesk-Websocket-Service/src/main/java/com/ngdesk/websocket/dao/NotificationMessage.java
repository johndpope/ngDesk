package com.ngdesk.websocket.dao;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class NotificationMessage {

	@JsonProperty("MODULE_ID")
	private String moduleId;

	@JsonProperty("DATA_ID")
	private String dataId;

	private String companyId;

	@JsonProperty("TYPE")
	private String type;

	public NotificationMessage() {

	}

	public NotificationMessage(String moduleId, String dataId, String companyId, String type) {
		super();
		this.moduleId = moduleId;
		this.dataId = dataId;
		this.companyId = companyId;
		this.type = type;
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

	@JsonIgnore
	public String getCompanyId() {
		return companyId;
	}

	@JsonProperty("COMPANY_ID")
	public void setCompanyId(String companyId) {
		this.companyId = companyId;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

}
