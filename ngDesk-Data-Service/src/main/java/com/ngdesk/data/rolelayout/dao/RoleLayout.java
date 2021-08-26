package com.ngdesk.data.rolelayout.dao;

import java.util.List;

import javax.validation.Valid;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ngdesk.commons.annotations.CustomNotEmpty;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.AccessMode;

public class RoleLayout {

	@JsonProperty("ROLE_LAYOUT_ID")
	@Id
	private String layoutId;

	@JsonProperty("COMPANY_ID")
	@Field("COMPANY_ID")
	private String companyId;

	@JsonProperty("ROLE")
	@Field("ROLE")
	private String role;

	@JsonProperty("NAME")
	@Field("NAME")
	private String name;

	@JsonProperty("DESCRIPTION")
	@Field("DESCRIPTION")
	private String description;

	@JsonProperty("MODULES")
	@Field("MODULES")
	@Valid
	private List<LayoutModule> modules;

	public RoleLayout(String layoutId, String companyId, String role, String name, String description,
			@Valid List<LayoutModule> modules) {
		super();
		this.layoutId = layoutId;
		this.companyId = companyId;
		this.role = role;
		this.name = name;
		this.description = description;
		this.modules = modules;
	}

	public RoleLayout() {
	}

	public String getLayoutId() {
		return layoutId;
	}

	public void setLayoutId(String layoutId) {
		this.layoutId = layoutId;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
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

	public List<LayoutModule> getModules() {
		return modules;
	}

	public void setModules(List<LayoutModule> modules) {
		this.modules = modules;
	}

	public String getCompanyId() {
		return companyId;
	}

	public void setCompanyId(String companyId) {
		this.companyId = companyId;
	}

}
