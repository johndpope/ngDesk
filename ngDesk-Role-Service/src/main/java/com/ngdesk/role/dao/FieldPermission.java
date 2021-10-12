package com.ngdesk.role.dao;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;

public class FieldPermission {

	@JsonProperty("FIELD")
	@NotEmpty(message = "FIELD_ID_REQUIRED")
	@Field("FIELD")
	private String fieldId;

	@JsonProperty("PERMISSION")
	@NotEmpty(message = "FIELD_PERMISSION_REQUIRED")
	@Pattern(regexp = "Read/Write|Not Set|Read|Write Only Creator|Not Editable|Write by team", message = "NOT_VALID_FIELD_PERMISSION_LEVEL")
	@Field("PERMISSION")
	private String permission;

	public FieldPermission() {

	}

	public FieldPermission(@NotEmpty(message = "FIELD_ID_REQUIRED") String fieldId,
			@NotEmpty(message = "FIELD_PERMISSION_REQUIRED") @Pattern(regexp = "Read/Write|Not Set|Read|Write Only Creator|Not Editable|Write by team", message = "NOT_VALID_FIELD_PERMISSION_LEVEL") String permission) {
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
