package com.ngdesk.sam.roles.dao;

import javax.validation.constraints.Pattern;

import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ngdesk.commons.annotations.CustomNotEmpty;

public class ModuleLevelPermission {

	@JsonProperty("ACCESS")
	@Field("ACCESS")
	@Pattern(regexp = "Enabled|Not Set|Disabled", message = "NOT_VALID_ACCESS")
	@CustomNotEmpty(message = "DAO_VARIABLE_REQUIRED", values = { "MODULE_PERMISSION_ACCESS" })
	private String access;

	@JsonProperty("ACCESS_TYPE")
	@Field("ACCESS_TYPE")
	@Pattern(regexp = "Normal|Not Set|Admin", message = "NOT_VALID_ACCESS_TYPE")
	@CustomNotEmpty(message = "DAO_VARIABLE_REQUIRED", values = { "MODULE_PERMISSION_ACCESS_TYPE" })
	private String accessType;

	@JsonProperty("EDIT")
	@Field("EDIT")
	@Pattern(regexp = "All|Not Set|None", message = "NOT_VALID_EDIT")
	@CustomNotEmpty(message = "DAO_VARIABLE_REQUIRED", values = { "MODULE_PERMISSION_EDIT" })
	private String edit;

	@JsonProperty("VIEW")
	@Field("VIEW")
	@Pattern(regexp = "All|Not Set|None", message = "NOT_VALID_VIEW")
	@CustomNotEmpty(message = "DAO_VARIABLE_REQUIRED", values = { "MODULE_PERMISSION_VIEW" })
	private String view;

	@JsonProperty("DELETE")
	@Field("DELETE")
	@Pattern(regexp = "All|Not Set|None", message = "NOT_VALID_DELETE")
	@CustomNotEmpty(message = "DAO_VARIABLE_REQUIRED", values = { "MODULE_PERMISSION_DELETE" })
	private String delete;

	public ModuleLevelPermission() {

	}

	public ModuleLevelPermission(
			@Pattern(regexp = "Enabled|Not Set|Disabled", message = "NOT_VALID_ACCESS") String access,
			@Pattern(regexp = "Normal|Not Set|Admin", message = "NOT_VALID_ACCESS_TYPE") String accessType,
			@Pattern(regexp = "All|Not Set|None", message = "NOT_VALID_EDIT") String edit,
			@Pattern(regexp = "All|Not Set|None", message = "NOT_VALID_VIEW") String view,
			@Pattern(regexp = "All|Not Set|None", message = "NOT_VALID_DELETE") String delete) {
		super();
		this.access = access;
		this.accessType = accessType;
		this.edit = edit;
		this.view = view;
		this.delete = delete;
	}

	public String getAccess() {
		return access;
	}

	public void setAccess(String access) {
		this.access = access;
	}

	public String getAccessType() {
		return accessType;
	}

	public void setAccessType(String accessType) {
		this.accessType = accessType;
	}

	public String getEdit() {
		return edit;
	}

	public void setEdit(String edit) {
		this.edit = edit;
	}

	public String getView() {
		return view;
	}

	public void setView(String view) {
		this.view = view;
	}

	public String getDelete() {
		return delete;
	}

	public void setDelete(String delete) {
		this.delete = delete;
	}

}
