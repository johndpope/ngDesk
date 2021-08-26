package com.ngdesk.schedules;

import java.util.Date;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.ngdesk.annotations.Timezone;

import io.swagger.annotations.ApiModelProperty;

public class Schedule {

	@ApiModelProperty(notes = "Expects name of schedule and cannot be null")
	@NotNull(message = "SCHEDULE_NAME_NOT_NULL")
	@Size(min = 1, message = "SCHEDULE_NAME_NOT_EMPTY")
	@NotBlank(message = "INVALID_SCHEDULE_NAME")
	private String name;

	private String description;

	@ApiModelProperty(notes = "Expects timezone string and cannot be null")
	@NotNull(message = "TIMEZONE_NOT_NULL")
	@Timezone
	private String timezone;

	private String userOncall;

	@ApiModelProperty(notes = "Expects list of layer objects")
	@NotNull(message = "LAYERS_NOT_NULL")
	@Size(min = 1, message = "LAYERS_NOT_EMPTY")
	@Valid
	private List<Layer> layers;

	@ApiModelProperty(notes = "The database generated schedule ID")
	private String scheduleId;

	private Date dateCreated;

	private Date dateUpdated;

	private String lastUpdatedBy;

	private String createdBy;

	public Schedule() {

	}

	public Schedule(
			@NotNull(message = "SCHEDULE_NAME_NOT_NULL") @Size(min = 1, message = "SCHEDULE_NAME_NOT_EMPTY") @NotBlank(message = "INVALID_SCHEDULE_NAME") String name,
			String description, @NotNull(message = "TIMEZONE_NOT_NULL") String timezone, String userOncall,
			@NotNull(message = "LAYERS_NOT_NULL") @Size(min = 1, message = "LAYERS_NOT_EMPTY") @Valid List<Layer> layers,
			String scheduleId, Date dateCreated, Date dateUpdated, String lastUpdatedBy, String createdBy) {
		super();
		this.name = name;
		this.description = description;
		this.timezone = timezone;
		this.userOncall = userOncall;
		this.layers = layers;
		this.scheduleId = scheduleId;
		this.dateCreated = dateCreated;
		this.dateUpdated = dateUpdated;
		this.lastUpdatedBy = lastUpdatedBy;
		this.createdBy = createdBy;
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

	public String getTimezone() {
		return timezone;
	}

	public void setTimezone(String timezone) {
		this.timezone = timezone;
	}

	public String getUserOncall() {
		return userOncall;
	}

	public void setUserOncall(String userOncall) {
		this.userOncall = userOncall;
	}

	public List<Layer> getLayers() {
		return layers;
	}

	public void setLayers(List<Layer> layers) {
		this.layers = layers;
	}

	public String getScheduleId() {
		return scheduleId;
	}

	public void setScheduleId(String scheduleId) {
		this.scheduleId = scheduleId;
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
