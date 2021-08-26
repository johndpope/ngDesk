package com.ngdesk.schedules;

import java.util.Date;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import com.ngdesk.annotations.ValidLayer;
import com.ngdesk.annotations.ValidTimes;

import io.swagger.annotations.ApiModelProperty;

@ValidLayer
public class Layer {

	@ApiModelProperty(notes = "Expects list of users")
	@NotNull(message = "USERS_NOT_NULL")
	@Size(min = 1, message = "USERS_EMPTY")
	private List<String> users;

	@ApiModelProperty(notes = "Expects rotation type eg: Weekly or Daily")
	@NotNull(message = "ROTATION_TYPE_NOT_NULL")
	@Pattern(regexp = "Weekly|Daily", message = "NOT_VALID_ROTATION_TYPE")
	private String rotationType;

	@ApiModelProperty(notes = "Expects layer's start time")
	@NotNull(message = "START_TIME_NOT_NULL")
	@ValidTimes
	private String startTime; 

	@ApiModelProperty(notes = "Expects layer's start date")
	@NotNull(message = "START_DATE_NOT_NULL")
	private Date startDate;

	@ApiModelProperty(notes = "Expects a boolean that represents whether it has restrictions or not")
	@NotNull(message = "HAS_RESTRICTIONS_NOT_NULL")
	private boolean hasRestrictions;

	@ApiModelProperty(notes = "Represents whether the restriction needs to be on specific times of the day or specific times of the week")
	@Pattern(regexp = "Day|Week", message = "INVALID_RESTRICTION_TYPE")
	private String restrictionType;

	@ApiModelProperty(notes = "Expects list of Restriction details having start time, end time, start day, end day")
	@NotNull(message = "RESTRICTIONS_NOT_NULL")
	@Valid
	private List<Restriction> restrictions;

	public Layer() {

	}

	public Layer(@NotNull(message = "USERS_NOT_NULL") @Size(min = 1, message = "USERS_EMPTY") List<String> users,
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
