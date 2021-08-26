package com.ngdesk.graphql.role.dao;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Role {

	@Id
	public String roleId;

	@Field("NAME")
	private String name;

	@Field("DESCRIPTION")
	private String description;

	@Field("PERMISSIONS")
	private List<Permission> permissions;

	public Role() {
	}

	public Role(String roleId, String name, String description, List<Permission> permissions) {
		super();
		this.roleId = roleId;
		this.name = name;
		this.description = description;
		this.permissions = permissions;
	}

	public String getRoleId() {
		return roleId;
	}

	public void setRoleId(String roleId) {
		this.roleId = roleId;
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
