package com.ngdesk.sam.roles.dao;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ngdesk.commons.annotations.CustomNotEmpty;

public class Role {

	@Id
	@JsonProperty("ROLE_ID")
	public String id;

	@JsonProperty("NAME")
	@Field("NAME")
	@CustomNotEmpty(message = "DAO_VARIABLE_REQUIRED", values = { "ROLE_NAME" })
	@Pattern(regexp = "([A-Za-z0-9]+)", message = "INVALID_ROLE_NAME")
	@Size(max = 30, message = "INVALID_ROLE_NAME_SIZE")
	private String name;

	@JsonProperty("DESCRIPTION")
	@Field("DESCRIPTION")
	private String description;

	@JsonProperty("PERMISSIONS")
	@Field("PERMISSIONS")
	@Valid
	@Size(min = 1, message = "ROLE_PERMISSION_REQUIRED")
	private List<Permission> permissions;

	public Role() {
	}

	public Role(String id,
			@NotEmpty(message = "ROLE_NAME_REQUIRED") @Pattern(regexp = "([A-Za-z0-9]+)", message = "INVALID_ROLE_NAME") @Size(max = 30, message = "INVALID_ROLE_NAME_SIZE") String name,
			String description,
			@Valid @Size(min = 1, message = "ROLE_PERMISSION_REQUIRED") List<Permission> permissions) {
		super();
		this.id = id;
		this.name = name;
		this.description = description;
		this.permissions = permissions;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public List<Permission> getPermissions() {
		return permissions;
	}

	public void setPermissions(List<Permission> permissions) {
		this.permissions = permissions;
	}

}
