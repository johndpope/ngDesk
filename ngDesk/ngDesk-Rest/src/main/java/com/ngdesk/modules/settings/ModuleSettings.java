package com.ngdesk.modules.settings;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ModuleSettings {

	@JsonProperty("PERMISSIONS")
	@NotNull(message = "PERMISSIONS_REQUIRED")
	@Valid
	Permissions permissions;

	public ModuleSettings() {

	}

	public ModuleSettings(@NotNull(message = "PERMISSIONS_REQUIRED") Permissions permissions) {
		super();
		this.permissions = permissions;
	}

	public Permissions getPermissions() {
		return permissions;
	}

	public void setPermissions(Permissions permissions) {
		this.permissions = permissions;
	}

}
