package com.ngdesk.dashboard;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ngdesk.modules.fields.Field;

public class DashBoardWidget {

	@JsonProperty("DASHBOARD_ID")
	private String dashboardId;

	@JsonProperty("NAME")
	@Size(min = 1, message = "NAME_NOT_EMPTY")
	public String name;

	@JsonProperty("DESCRIPTION")
	public String description;

	@JsonProperty("TEAMS")
	public List<String> teams = new ArrayList<>();

	@JsonProperty("WIDGETS")
	private List<Widget> widgets = new ArrayList<Widget>();

	@JsonProperty("DATE_CREATED")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
	private Timestamp dateCreated;

	@JsonProperty("DATE_UPDATED")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
	private Timestamp dateUpdated;

	@JsonProperty("LAST_UPDATED_BY")
	private String lastUpdatedBy;

	@JsonProperty("CREATED_BY")
	private String createdBy;

	@JsonProperty("DEFAULT")
	private String isDefault;

	public DashBoardWidget() {

	}

	public DashBoardWidget(String dashboardId,
			@NotNull(message = "NAME_NOT_NULL") @Size(min = 1, message = "NAME_NOT_EMPTY") String name,
			String description, List<String> teams, List<Widget> widgets, Timestamp dateCreated, Timestamp dateUpdated,
			String lastUpdatedBy, String createdBy, String isDefault) {
		super();
		this.dashboardId = dashboardId;
		this.name = name;
		this.description = description;
		this.teams = teams;
		this.widgets = widgets;
		this.dateCreated = dateCreated;
		this.dateUpdated = dateUpdated;
		this.lastUpdatedBy = lastUpdatedBy;
		this.createdBy = createdBy;
		this.isDefault = isDefault;
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

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public List<String> getTeams() {
		return teams;
	}

	public void setTeams(List<String> teams) {
		this.teams = teams;
	}

	public List<Widget> getWidgets() {
		return widgets;
	}

	public void setWidgets(List<Widget> widgets) {
		this.widgets = widgets;
	}

	public Timestamp getDateCreated() {
		return dateCreated;
	}

	public void setDateCreated(Timestamp dateCreated) {
		this.dateCreated = dateCreated;
	}

	public Timestamp getDateUpdated() {
		return dateUpdated;
	}

	public void setDateUpdated(Timestamp dateUpdated) {
		this.dateUpdated = dateUpdated;
	}

	public String getLastUpdatedBy() {
		return lastUpdatedBy;
	}

	public void setLastUpdatedBy(String lastUpdatedBy) {
		this.lastUpdatedBy = lastUpdatedBy;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public String getIsDefault() {
		return isDefault;
	}

	public void setIsDefault(String isDefault) {
		this.isDefault = isDefault;
	}

}
