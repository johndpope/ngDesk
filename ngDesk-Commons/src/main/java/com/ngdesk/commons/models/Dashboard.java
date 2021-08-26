package com.ngdesk.commons.models;

import java.util.Date;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.springframework.data.annotation.Id;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.ngdesk.commons.annotations.CustomNotEmpty;

public class Dashboard {

	@Id
	private String dashboardId;

	@CustomNotEmpty(message = "DAO_VARIABLE_REQUIRED", values = { "DASHBOARD_NAME" })
	private String name;

	@CustomNotEmpty(message = "DAO_VARIABLE_REQUIRED", values = { "DASHBOARD_ROLE" })
	private String role;

	private String description;

	@NotNull(message = "WIDGETS_REQUIRED")
	@Size(min = 1, message = "WIDGETS_SIZE_INVALID")
	@Valid
	private List<Widget> widgets;

	@JsonIgnore
	private String companyId;

	private Date dateCreated;

	private Date dateUpdated;

	private String createdBy;

	private String lastUpdatedBy;

	Dashboard() {

	}

	public Dashboard(String dashboardId, String name, String role, String description,
			@NotNull(message = "WIDGETS_REQUIRED") @Size(min = 1, message = "WIDGETS_SIZE_INVALID") @Valid List<Widget> widgets,
			String companyId, Date dateCreated, Date dateUpdated, String createdBy, String lastUpdatedBy) {
		super();
		this.dashboardId = dashboardId;
		this.name = name;
		this.role = role;
		this.description = description;
		this.widgets = widgets;
		this.companyId = companyId;
		this.dateCreated = dateCreated;
		this.dateUpdated = dateUpdated;
		this.createdBy = createdBy;
		this.lastUpdatedBy = lastUpdatedBy;
	}

	public String getDashboardId() {
		return dashboardId;
	}

	public void setDashboardId(String dashboardId) {
		this.dashboardId = dashboardId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public List<Widget> getWidgets() {
		return widgets;
	}

	public void setWidgets(List<Widget> widgets) {
		this.widgets = widgets;
	}

	public String getCompanyId() {
		return companyId;
	}

	public void setCompanyId(String companyId) {
		this.companyId = companyId;
	}

	public Date getDateCreated() {
		return dateCreated;
	}

	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}

	public Date getDateUpdated() {
		return dateUpdated;
	}

	public void setDateUpdated(Date dateUpdated) {
		this.dateUpdated = dateUpdated;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public String getLastUpdatedBy() {
		return lastUpdatedBy;
	}

	public void setLastUpdatedBy(String lastUpdatedBy) {
		this.lastUpdatedBy = lastUpdatedBy;
	}

}
