package com.ngdesk.sam.roles.dao;

import javax.validation.constraints.Pattern;

import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ngdesk.commons.annotations.CustomNotEmpty;

public class FieldPermission {

	@JsonProperty("FIELD")
	@Field("FIELD")
	@CustomNotEmpty(message = "DAO_VARIABLE_REQUIRED", values = { "FIELD_ID" })
	private String fieldId;

	@JsonProperty("PERMISSION")
	@Field("PERMISSION")
	@CustomNotEmpty(message = "DAO_VARIABLE_REQUIRED", values = { "FIELD_PERMISSION" })
	@Pattern(regexp = "Read/Write|Not Set|Read", message = "NOT_VALID_FIELD_PERMISSION_LEVEL")
	private String permission;

	public FieldPermission() {

	}

	public FieldPermission(String fieldId,
			@Pattern(regexp = "Read/Write|Not Set|Read", message = "NOT_VALID_FIELD_PERMISSION_LEVEL") String permission) {
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
