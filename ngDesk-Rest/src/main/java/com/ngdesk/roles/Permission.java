package com.ngdesk.roles;

import java.util.List;


import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
@JsonIgnoreProperties("_class")
public class Permission {

	@JsonProperty("MODULE")
	@NotEmpty(message = "MODULE_PERMISSION_REQUIRED")
	private String module;

	@JsonProperty("MODULE_PERMISSIONS")
	@NotNull(message = "MODULE_LEVEL_PERMISSIONS_REQUIRED")
	@Valid
	private ModuleLevelPermission modulePermission;

	@JsonProperty("FIELD_PERMISSIONS")
	@NotNull(message = "FIELD_LEVEL_PERMISSIONS_REQUIRED")
	private List<FieldPermission> fieldPermissions;

	public Permission() {

	}

	public Permission(@NotEmpty(message = "MODULE_PERMISSION_REQUIRED") String module,
			@NotNull(message = "MODULE_LEVEL_PERMISSIONS_REQUIRED") @Valid ModuleLevelPermission modulePermission,
			@NotEmpty(message = "FIELD_LEVEL_PERMISSIONS_REQUIRED") List<FieldPermission> fieldPermissions) {
		super();
		this.module = module;
		this.modulePermission = modulePermission;
		this.fieldPermissions = fieldPermissions;
	}

	public String getModule() {
		return module;
	}

	public void setModule(String module) {
		this.module = module;
	}

	public ModuleLevelPermission getModulePermission() {
		return modulePermission;
	}

	public void setModulePermission(ModuleLevelPermission modulePermission) {
		this.modulePermission = modulePermission;
	}

	public List<FieldPermission> getFieldPermissions() {
		return fieldPermissions;
	}

	public void setFieldPermissions(List<FieldPermission> fieldPermissions) {
		this.fieldPermissions = fieldPermissions;
	}

}
