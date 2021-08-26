package com.ngdesk.role.dao;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Role {

	@JsonProperty("ROLE_ID")
	@Id
	public String id;

	@JsonProperty("NAME")
	@Field("NAME")
	private String name;

	@JsonProperty("DESCRIPTION")
	@Field("DESCRIPTION")
	private String description;

	@JsonProperty("PERMISSIONS")
	@Field("PERMISSIONS")
	private List<Permission> permissions;

	public Role() {
	}

	public Role(String id, String name, String description, List<Permission> permissions) {
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
