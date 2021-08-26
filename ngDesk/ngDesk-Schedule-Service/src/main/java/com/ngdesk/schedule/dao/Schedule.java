package com.ngdesk.schedule.dao;

import java.util.Date;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ngdesk.commons.annotations.CustomTimeZoneValidation;

public class Schedule {

	@Id
	@JsonProperty("SCHEDULE_ID")
	private String id;

	@Field("NAME")
	@JsonProperty("NAME")
	@NotEmpty(message = "SCHEDULE_NAME_NOT_EMPTY")
	@NotBlank(message = "INVALID_SCHEDULE_NAME")
	private String name;

	@JsonProperty("DESCRIPTION")
	@Field("DESCRIPTION")
	private String description;

	@Field("TIMEZONE")
	@JsonProperty("TIMEZONE")
	@NotNull(message = "TIMEZONE_NOT_NULL")
	@CustomTimeZoneValidation(message = "INVALID_TIMEZONE", values = { "SCHEDULE_TIMEZONE" })
	private String timezone;

	@JsonProperty("USER_ON_CALL")
	@Field("USER_ON_CALL")
	private String userOncall;

	@JsonProperty("LAYERS")
	@Field("LAYERS")
	@NotNull(message = "LAYERS_NOT_NULL")
	@NotEmpty(message = "LAYERS_NOT_EMPTY")
	@Valid
	private List<Layer> layers;

	@JsonProperty("SCHEDULE_ID")
	@Field("SCHEDULE_ID")
	private String scheduleId;

	@JsonProperty("DATE_CREATED")
	@Field("DATE_CREATED")
	private Date dateCreated;

	@JsonProperty("DATE_UPDATED")
	@Field("DATE_UPDATED")
	private Date dateUpdated;

	@JsonProperty("CREATED_BY")
	@Field("CREATED_BY")
	private String createdBy;

	@JsonProperty("LAST_UPDATED_BY")
	@Field("LAST_UPDATED_BY")
	private String lastUpdatedBy;

	public Schedule() {

	}

	public Schedule(String id,
			@NotEmpty(message = "SCHEDULE_NAME_NOT_EMPTY") @NotBlank(message = "INVALID_SCHEDULE_NAME") String name,
			String description, @NotNull(message = "TIMEZONE_NOT_NULL") String timezone, String userOncall,
			@NotNull(message = "LAYERS_NOT_NULL") @NotEmpty(message = "LAYERS_NOT_EMPTY") @Valid List<Layer> layers,
			String scheduleId, Date dateCreated, Date dateUpdated, String createdBy, String lastUpdatedBy) {
		super();
		this.id = id;
		this.name = name;
		this.description = description;
		this.timezone = timezone;
		this.userOncall = userOncall;
		this.layers = layers;
		this.scheduleId = scheduleId;
		this.dateCreated = dateCreated;
		this.dateUpdated = dateUpdated;
		this.createdBy = createdBy;
		this.lastUpdatedBy = lastUpdatedBy;
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
