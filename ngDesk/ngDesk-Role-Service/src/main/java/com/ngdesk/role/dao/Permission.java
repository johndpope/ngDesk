package com.ngdesk.role.dao;

import java.util.List;

import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Permission {

	@JsonProperty("MODULE")
	@Field("MODULE")
	private String module;

	@Field("MODULE_PERMISSIONS")
	@JsonProperty("MODULE_PERMISSIONS")
	private ModuleLevelPermission modulePermission;

	@JsonProperty("FIELD_PERMISSIONS")
	@Field("FIELD_PERMISSIONS")
	private List<FieldPermission> fieldPermissions;

	public Permission() {

	}

	
	public Permission(String module, ModuleLevelPermission modulePermission, List<FieldPermission> fieldPermissions) {
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
