package com.ngdesk.company.rolelayout.dao;

import java.util.List;

import javax.validation.Valid;

import org.springframework.data.annotation.Id;

import com.ngdesk.commons.annotations.CustomNotEmpty;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.AccessMode;

public class RoleLayout {

	@Schema(required = false, accessMode = AccessMode.READ_ONLY, description = "Autogenerated Id")
	@Id
	private String layoutId;

	@Schema(required = false, accessMode = AccessMode.READ_ONLY, description = "company id for the layout")
	private String companyId;

	@Schema(required = true, description = "Role id for the layout")
	@CustomNotEmpty(message = "DAO_VARIABLE_REQUIRED", values = { "ROLE" })
	private String role;

	@Schema(required = true, description = "Name of the layout", example = "Active Process")
	@CustomNotEmpty(message = "DAO_VARIABLE_REQUIRED", values = { "ROLE_LAYOUT_NAME" })
	private String name;

	@Schema(description = "Description of the Layout", required = false, example = "Layout to view module")
	private String description;

	@Schema(description = "Default layout for the role", required = true)
	private boolean defaultLayout;

	@CustomNotEmpty(message = "DAO_VARIABLE_REQUIRED", values = { "ROLE_LAYOUT_MODULE" })
	@Valid
	private List<Tab> tabs;

	public RoleLayout(String layoutId, String companyId, String role, String name, String description,
			boolean defaultLayout, @Valid List<Tab> tabs) {
		super();
		this.layoutId = layoutId;
		this.companyId = companyId;
		this.role = role;
		this.name = name;
		this.description = description;
		this.defaultLayout = defaultLayout;
		this.tabs = tabs;
	}

	public RoleLayout() {
		super();
	}

	public String getLayoutId() {
		return layoutId;
	}

	public void setLayoutId(String layoutId) {
		this.layoutId = layoutId;
	}

	public String getCompanyId() {
		return companyId;
	}

	public void setCompanyId(String companyId) {
		this.companyId = companyId;
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

	public boolean isDefaultLayout() {
		return defaultLayout;
	}

	public void setDefaultLayout(boolean defaultLayout) {
		this.defaultLayout = defaultLayout;
	}

	public List<Tab> getTabs() {
		return tabs;
	}

	public void setTabs(List<Tab> tabs) {
		this.tabs = tabs;
	}

}
