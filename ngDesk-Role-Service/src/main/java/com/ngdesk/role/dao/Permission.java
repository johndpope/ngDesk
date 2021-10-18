package com.ngdesk.role.dao;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;

public class Permission {

	@Schema(description = "MODULE")
	@JsonProperty("MODULE")
	@NotEmpty(message = "MODULE_PERMISSION_REQUIRED")
	@Field("MODULE")
	private String module;

	@Schema(description = "MODULE PERMISSIONS")
	@JsonProperty("MODULE_PERMISSIONS")
	@NotNull(message = "MODULE_LEVEL_PERMISSIONS_REQUIRED")
	@Valid
	@Field("MODULE_PERMISSIONS")
	private ModuleLevelPermission modulePermission;

	@Schema(description = "FIELD PERMISSIONS")
	@JsonProperty("FIELD_PERMISSIONS")
	@NotNull(message = "FIELD_LEVEL_PERMISSIONS_REQUIRED")
	@Field("FIELD_PERMISSIONS")
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
