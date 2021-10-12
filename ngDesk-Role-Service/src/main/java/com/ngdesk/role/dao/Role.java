package com.ngdesk.role.dao;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Role {

	@JsonProperty("NAME")
	@NotEmpty(message = "ROLE_NAME_REQUIRED")
	@Pattern(regexp = "([A-Za-z0-9]+)", message = "INVALID_ROLE_NAME")
	@Size(max = 30, message = "INVALID_ROLE_NAME_SIZE")
	@Field("NAME")
	private String name;

	@JsonProperty("DESCRIPTION")
	@Field("DESCRIPTION")
	private String description;

	@JsonProperty("PERMISSIONS")
	@Valid
	@Size(min = 1, message = "ROLE_PERMISSION_REQUIRED")
	@Field("PERMISSIONS")
	private List<Permission> permissions;

	@JsonProperty("ROLE_ID")
	public String id;

	public Role() {

	}

	public Role(
			@NotEmpty(message = "ROLE_NAME_REQUIRED") @Pattern(regexp = "([A-Za-z0-9]+)", message = "INVALID_ROLE_NAME") @Size(max = 30, message = "INVALID_ROLE_NAME_SIZE") String name,
			String description,
			@Valid @Size(min = 1, message = "ROLE_PERMISSION_REQUIRED") List<Permission> permissions, String id) {
		super();
		this.name = name;
		this.description = description;
		this.permissions = permissions;
		this.id = id;
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
