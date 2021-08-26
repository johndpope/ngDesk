package com.ngdesk.escalations;


import java.util.List;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModelProperty;

public class EscalateTo {

	@ApiModelProperty(notes = "Expects list of Schedule ids and cannot be null")
	@JsonProperty("SCHEDULE_IDS")
	@NotNull(message = "SCHEDULE_IDS_NOT_NULL")
	private List<String> scheduleIds;

	@ApiModelProperty(notes = "Expects list of userIds and cannot be null")
	@JsonProperty("USER_IDS")
	@NotNull(message = "USER_IDS_NOT_NULL")
	private List<String> userIds;

	@ApiModelProperty(notes = "Expects list of team ids and cannot be null")
	@JsonProperty("TEAM_IDS")
	@NotNull(message = "TEAMS_IDS_NOT_NULL")
	private List<String> teamIds;

	public EscalateTo() {

	}

	public EscalateTo(@NotNull(message = "SCHEDULE_IDS_NOT_NULL") List<String> scheduleIds,
			@NotNull(message = "USER_IDS_NOT_NULL") List<String> userIds,
			@NotNull(message = "TEAMS_IDS_NOT_NULL") List<String> teamIds) {
		super();
		this.scheduleIds = scheduleIds;
		this.userIds = userIds;
		this.teamIds = teamIds;
	}

	public List<String> getScheduleIds() {
		return scheduleIds;
	}

	public void setScheduleIds(List<String> scheduleIds) {
		this.scheduleIds = scheduleIds;
	}

	public List<String> getUserIds() {
		return userIds;
	}

	public void setUserIds(List<String> userIds) {
		this.userIds = userIds;
	}

	public List<String> getTeamIds() {
		return teamIds;
	}

	public void setTeamIds(List<String> teamIds) {
		this.teamIds = teamIds;
	}

}
