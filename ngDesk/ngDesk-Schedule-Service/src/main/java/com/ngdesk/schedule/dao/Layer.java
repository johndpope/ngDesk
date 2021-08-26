package com.ngdesk.schedule.dao;

import java.util.Date;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

// @ValidLayer
public class Layer {

	// @ApiModelProperty(notes = "Expects list of users")
	@Field("USERS")
	@JsonProperty("USERS")
	@NotNull(message = "USERS_NOT_NULL")
	@NotEmpty(message = "USERS_EMPTY")
	private List<String> users;

	// @ApiModelProperty(notes = "Expects rotation type eg: Weekly or Daily")
	@Field("ROTATION_TYPE")
	@JsonProperty("ROTATION_TYPE")
	@NotNull(message = "ROTATION_TYPE_NOT_NULL")
	@Pattern(regexp = "Weekly|Daily", message = "NOT_VALID_ROTATION_TYPE")
	private String rotationType;

	// @ApiModelProperty(notes = "Expects layer's start time")
	@Field("START_TIME")
	@JsonProperty("START_TIME")
	@NotNull(message = "START_TIME_NOT_NULL")
	// @ValidTimes
	private String startTime;

	// @ApiModelProperty(notes = "Expects layer's start date")
	@Field("START_DATE")
	@JsonProperty("START_DATE")
	@NotNull(message = "START_DATE_NOT_NULL")
	private Date startDate;

	// @ApiModelProperty(notes = "Expects a boolean that represents whether it has
	// restrictions or not")
	@Field("HAS_RESTRICTIONS")
	@JsonProperty("HAS_RESTRICTIONS")
	@NotNull(message = "HAS_RESTRICTIONS_NOT_NULL")
	private boolean hasRestrictions;

	// @ApiModelProperty(notes = "Represents whether the restriction needs to be on
	// specific times of the day or specific times of the week")
	@Field("RESTRICTION_TYPE")
	@JsonProperty("RESTRICTION_TYPE")
	@Pattern(regexp = "Day|Week", message = "INVALID_RESTRICTION_TYPE")
	private String restrictionType;

	// @ApiModelProperty(notes = "Expects list of Restriction details having start
	// time, end time, start day, end day")
	@Field("LAYER_RESTRICTIONS")
	@JsonProperty("LAYER_RESTRICTIONS")
	@NotNull(message = "RESTRICTIONS_NOT_NULL")
	@Valid
	private List<Restriction> restrictions;

	public Layer() {

	}

	public Layer(@NotNull(message = "USERS_NOT_NULL") @NotEmpty(message = "USERS_EMPTY") List<String> users,
			@NotNull(message = "ROTATION_TYPE_NOT_NULL") @Pattern(regexp = "Weekly|Daily", message = "NOT_VALID_ROTATION_TYPE") String rotationType,
			@NotNull(message = "START_TIME_NOT_NULL") String startTime,
			@NotNull(message = "START_DATE_NOT_NULL") Date startDate,
			@NotNull(message = "HAS_RESTRICTIONS_NOT_NULL") boolean hasRestrictions,
			@Pattern(regexp = "Day|Week", message = "INVALID_RESTRICTION_TYPE") String restrictionType,
			@NotNull(message = "RESTRICTIONS_NOT_NULL") @Valid List<Restriction> restrictions) {
		super();
		this.users = users;
		this.rotationType = rotationType;
		this.startTime = startTime;
		this.startDate = startDate;
		this.hasRestrictions = hasRestrictions;
		this.restrictionType = restrictionType;
		this.restrictions = restrictions;
	}

	public List<String> getUsers() {
		return users;
	}

	public void setUsers(List<String> users) {
		this.users = users;
	}

	public String getRotationType() {
		return rotationType;
	}

	public void setRotationType(String rotationType) {
		this.rotationType = rotationType;
	}

	public String getStartTime() {
		return startTime;
	}

	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public boolean isHasRestrictions() {
		return hasRestrictions;
	}

	public void setHasRestrictions(boolean hasRestrictions) {
		this.hasRestrictions = hasRestrictions;
	}

	public String getRestrictionType() {
		return restrictionType;
	}

	public void setRestrictionType(String restrictionType) {
		this.restrictionType = restrictionType;
	}

	public List<Restriction> getRestrictions() {
		return restrictions;
	}

	public void setRestrictions(List<Restriction> restrictions) {
		this.restrictions = restrictions;
	}

}
