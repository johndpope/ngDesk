package com.ngdesk.sidebar.dao;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ngdesk.commons.annotations.CustomNotEmpty;
import com.ngdesk.commons.annotations.CustomNotNull;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.AccessMode;

public class CustomSidebar {
	
	@Schema(required = false, accessMode = AccessMode.READ_ONLY, description = "Autogenerated Id")
	@Id
	private String id;
	
	@Schema(description = "Sidebar", required = true)
	@JsonProperty("SIDE_BAR")
	@Field("SIDE_BAR")
	@CustomNotNull(message = "NOT_NULL", values = { "CUSTOM_SIDEBAR"})
	private Sidebar sidebar;
	
	@Schema(description = "Company ID", required = true)
	@Field("COMPANY_ID")
	@JsonProperty("COMPANY_ID")
	@CustomNotEmpty(message = "DAO_VARIABLE_REQUIRED", values = { "CUSTOM_SIDEBAR_COMPANY_ID" })
	private String companyId;

	public CustomSidebar() {

	}

	public CustomSidebar(String id, Sidebar sidebar, String companyId) {
		super();
		this.id = id;
		this.sidebar = sidebar;
		this.companyId = companyId;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Sidebar getSidebar() {
		return sidebar;
	}

	public void setSidebar(Sidebar sidebar) {
		this.sidebar = sidebar;
	}

	public String getCompanyId() {
		return companyId;
	}

	public void setCompanyId(String companyId) {
		this.companyId = companyId;
	}
	
	
}
