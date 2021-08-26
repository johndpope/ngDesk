package com.ngdesk.modules.monitors;

import java.util.List;
import java.sql.Timestamp;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.springframework.beans.factory.annotation.Required;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ngdesk.modules.rules.Action;
import com.ngdesk.modules.rules.Condition;
import com.fasterxml.jackson.annotation.JsonFormat;

public class ModuleMonitor {

	@JsonProperty("MONITOR_ID")
	private String monitorId;

	@JsonProperty("NAME")
	@NotNull(message = "MONITOR_NAME_NOT_NULL")
	@Size(min = 1, message = "MONITOR_NAME_NOT_EMPTY")
	private String name;

	@JsonProperty("DESCRIPTION")
	@NotNull(message = "DESCRIPTION_NOT_NULL")
	private String description;

	@JsonProperty("INTERVAL")
	@NotNull(message = "INTERVAL_NOT_NULL")
	@Min(value = 1, message = "INTERVAL_NOT_EMPTY")
	private int interval;

	@JsonProperty("CONDITIONS")
	@NotNull(message = "CONDITIONS_NOT_NULL")
	@Size(min = 1, message = "CONDITIONS_NOT_EMPTY")
	@Valid
	private List<Condition> conditions;

	@JsonProperty("ACTIONS")
	@NotNull(message = "ACTIONS_NOT_NULL")
	@Size(min = 1, message = "ACTIONS_NOT_EMPTY")
	@Valid
	private List<MonitorAction> monitoractions;

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

	public ModuleMonitor() {

	}

	public ModuleMonitor(String monitorId,
			@NotNull(message = "MONITOR_NAME_NOT_NULL") @Size(min = 1, message = "MONITOR_NAME_NOT_EMPTY") String name,
			@NotNull(message = "DESCRIPTION_NOT_NULL") String description,
			@NotNull(message = "INTERVAL_NOT_NULL") @Min(value = 1, message = "INTERVAL_NOT_EMPTY") int interval,
			@NotNull(message = "CONDITIONS_NOT_NULL") @Size(min = 1, message = "CONDITIONS_NOT_EMPTY") @Valid List<Condition> conditions,
			@NotNull(message = "ACTIONS_NOT_NULL") @Size(min = 1, message = "ACTIONS_NOT_EMPTY") @Valid List<MonitorAction> monitoractions,
			Timestamp dateCreated, Timestamp dateUpdated, String lastUpdatedBy, String createdBy) {
		super();
		this.monitorId = monitorId;
		this.name = name;
		this.description = description;
		this.interval = interval;
		this.conditions = conditions;
		this.monitoractions = monitoractions;
		this.dateCreated = dateCreated;
		this.dateUpdated = dateUpdated;
		this.lastUpdatedBy = lastUpdatedBy;
		this.createdBy = createdBy;
	}

	public String getMonitorId() {
		return monitorId;
	}

	public void setMonitorId(String monitorId) {
		this.monitorId = monitorId;
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

	public int getInterval() {
		return interval;
	}

	public void setInterval(int interval) {
		this.interval = interval;
	}

	public List<Condition> getConditions() {
		return conditions;
	}

	public void setConditions(List<Condition> conditions) {
		this.conditions = conditions;
	}

	public List<MonitorAction> getMonitoractions() {
		return monitoractions;
	}

	public void setMonitoractions(List<MonitorAction> monitoractions) {
		this.monitoractions = monitoractions;
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

}
