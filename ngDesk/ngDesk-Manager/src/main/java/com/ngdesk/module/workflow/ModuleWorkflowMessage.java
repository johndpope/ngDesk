package com.ngdesk.module.workflow;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ModuleWorkflowMessage {

	@JsonProperty("COMPANY_UUID")
	private String companyUUID;

	@JsonProperty("MODULE")
	private String module;

	@JsonProperty("DATA_ID")
	private String dataId;

	@JsonProperty("USER_UUID")
	private String userUUID;

	@JsonProperty("OLD_COPY")
	private Map<String, Object> oldCopy;

	public ModuleWorkflowMessage() {

	}

	public ModuleWorkflowMessage(String companyUUID, String module, String dataId, String userUUID,
			Map<String, Object> oldCopy) {
		super();
		this.companyUUID = companyUUID;
		this.module = module;
		this.dataId = dataId;
		this.userUUID = userUUID;
		this.oldCopy = oldCopy;
	}

	public String getCompanyUUID() {
		return companyUUID;
	}

	public void setCompanyUUID(String companyUUID) {
		this.companyUUID = companyUUID;
	}

	public String getModule() {
		return module;
	}

	public void setModule(String module) {
		this.module = module;
	}

	public String getDataId() {
		return dataId;
	}

	public void setDataId(String dataId) {
		this.dataId = dataId;
	}

	public String getUserUUID() {
		return userUUID;
	}

	public void setUserUUID(String userUUID) {
		this.userUUID = userUUID;
	}

	public Map<String, Object> getOldCopy() {
		return oldCopy;
	}

	public void setOldCopy(Map<String, Object> oldCopy) {
		this.oldCopy = oldCopy;
	}

}
