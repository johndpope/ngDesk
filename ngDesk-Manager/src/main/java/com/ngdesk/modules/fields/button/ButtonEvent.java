package com.ngdesk.modules.fields.button;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ButtonEvent {
	@JsonProperty("COMPANY_UUID")
	private String companyUuid;

	@JsonProperty("MODULE_ID")
	private String moduleId;

	@JsonProperty("ENTRY_ID")
	private String entryId;

	@JsonProperty("FIELD_ID")
	private String fieldId;

	@JsonProperty("USER_UUID")
	private String userUuid;

	public ButtonEvent(String companyUuid, String moduleId, String entryId, String fieldId, String userUuid) {
		super();
		this.companyUuid = companyUuid;
		this.moduleId = moduleId;
		this.entryId = entryId;
		this.fieldId = fieldId;
		this.userUuid = userUuid;
	}

	public String getCompanyUuid() {
		return companyUuid;
	}

	public void setCompanyUuid(String companyUuid) {
		this.companyUuid = companyUuid;
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

	public String getFieldId() {
		return fieldId;
	}

	public void setFieldId(String fieldId) {
		this.fieldId = fieldId;
	}

	public String getUserUuid() {
		return userUuid;
	}

	public void setUserUuid(String userUuid) {
		this.userUuid = userUuid;
	}

}
