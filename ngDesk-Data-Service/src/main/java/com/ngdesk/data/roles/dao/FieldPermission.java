package com.ngdesk.data.roles.dao;

import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;

public class FieldPermission {

	@JsonProperty("FIELD")
	@Field("FIELD")
	private String fieldId;

	@JsonProperty("PERMISSION")
	@Field("PERMISSION")
	private String permission;

	public FieldPermission() {

	}

	public FieldPermission(String fieldId, String permission) {
		super();
		this.fieldId = fieldId;
		this.permission = permission;
	}

	public String getFieldId() {
		return fieldId;
	}

	public void setFieldId(String fieldId) {
		this.fieldId = fieldId;
	}

	public String getPermission() {
		return permission;
	}

	public void setPermission(String permission) {
		this.permission = permission;
	}

}
