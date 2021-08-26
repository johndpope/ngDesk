package com.ngdesk.sam.roles.dao;

import java.util.List;

import javax.validation.Valid;

import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ngdesk.commons.annotations.CustomNotEmpty;
import com.ngdesk.commons.annotations.CustomNotNull;

public class Permission {

	@JsonProperty("MODULE")
	@Field("MODULE")
	@CustomNotEmpty(message = "DAO_VARIABLE_REQUIRED", values = { "MODULE" })
	private String module;

	@Field("MODULE_PERMISSIONS")
	@JsonProperty("MODULE_PERMISSIONS")
	@CustomNotNull(message = "NOT_NULL", values = { "MODULE_LEVEL_PERMISSIONS" })
	@Valid
	private ModuleLevelPermission modulePermission;

	@JsonProperty("FIELD_PERMISSIONS")
	@Field("FIELD_PERMISSIONS")
	@CustomNotNull(message = "NOT_NULL", values = { "FIELD_PERMISSION" })
	private List<FieldPermission> fieldPermissions;

	public Permission() {

	}

	public Permission(String module, @Valid ModuleLevelPermission modulePermission,
			List<FieldPermission> fieldPermissions) {
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
