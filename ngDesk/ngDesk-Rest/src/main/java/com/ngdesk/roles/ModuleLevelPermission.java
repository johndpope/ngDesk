package com.ngdesk.roles;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ModuleLevelPermission {

	@JsonProperty("ACCESS")
	@Pattern(regexp = "Enabled|Not Set|Disabled", message = "NOT_VALID_ACCESS")
	@NotEmpty(message = "ACCESS_REQUIRED")
	private String access;

	@JsonProperty("ACCESS_TYPE")
	@Pattern(regexp = "Normal|Not Set|Admin", message = "NOT_VALID_ACCESS_TYPE")
	@NotEmpty(message = "ACCESS_TYPE_REQUIRED")
	private String accessType;

	@JsonProperty("EDIT")
	@Pattern(regexp = "All|Not Set|None", message = "NOT_VALID_EDIT")
	@NotEmpty(message = "MODULE_PERMISSION_EDIT_REQUIRED")
	private String edit;

	@JsonProperty("VIEW")
	@Pattern(regexp = "All|Not Set|None", message = "NOT_VALID_VIEW")
	@NotEmpty(message = "MODULE_PERMISSION_VIEW_REQUIRED")
	private String view;

	@JsonProperty("DELETE")
	@Pattern(regexp = "All|Not Set|None", message = "NOT_VALID_DELETE")
	@NotEmpty(message = "MODULE_PERMISSION_DELETE_REQUIRED")
	private String delete;

	public ModuleLevelPermission() {

	}

	public ModuleLevelPermission(
			@Pattern(regexp = "Enabled|Not Set|Disabled", message = "NOT_VALID_ACCESS") @NotEmpty(message = "ACCESS_REQUIRED") String access,
			@Pattern(regexp = "Normal|Not Set|Admin", message = "NOT_VALID_ACCESS_TYPE") @NotEmpty(message = "ACCESS_TYPE_REQUIRED") String accessType,
			@Pattern(regexp = "All|Not Set|None", message = "NOT_VALID_EDIT") @NotEmpty(message = "MODULE_PERMISSION_EDIT_REQUIRED") String edit,
			@Pattern(regexp = "All|Not Set|None", message = "NOT_VALID_VIEW") @NotEmpty(message = "MODULE_PERMISSION_VIEW_REQUIRED") String view,
			@Pattern(regexp = "All|Not Set|None", message = "NOT_VALID_DELETE") @NotEmpty(message = "MODULE_PERMISSION_DELETE_REQUIRED") String delete) {
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
