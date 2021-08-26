package com.ngdesk.graphql.role.layout.dao;

import java.util.List;

import org.springframework.data.annotation.Id;

public class RoleLayout {

	@Id
	private String layoutId;

	private String companyId;

	private String role;

	private String name;

	private String description;

	private boolean defaultLayout;

	private List<Tab> tabs;

	public RoleLayout() {
	}

	public RoleLayout(String layoutId, String companyId, String role, String name, String description,
			boolean defaultLayout, List<Tab> tabs) {
		super();
		this.layoutId = layoutId;
		this.companyId = companyId;
		this.role = role;
		this.name = name;
		this.description = description;
		this.defaultLayout = defaultLayout;
		this.tabs = tabs;
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
