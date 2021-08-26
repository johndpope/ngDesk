package com.ngdesk.escalation.dao;

import java.util.List;

import javax.validation.constraints.NotNull;

import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ngdesk.commons.annotations.CustomNotNull;

import io.swagger.v3.oas.annotations.media.Schema;

public class EscalateTo {

	@Schema(description = "List of schedule Ids")
	@JsonProperty("SCHEDULE_IDS")
	@Field("SCHEDULE_IDS")
	@CustomNotNull(message = "NOT_NULL", values = { "SCHEDULE_IDS" })
	private List<String> scheduleIds;
	
	@Schema(description = "List of user Ids")
	@JsonProperty("USER_IDS")
	@Field("USER_IDS")
	@CustomNotNull(message = "NOT_NULL", values = { "USER_IDS" })
	private List<String> userIds;

	@Schema(description = "List of team Ids")
	@JsonProperty("TEAM_IDS")
	@Field("TEAM_IDS")
	@CustomNotNull(message = "NOT_NULL", values = { "TEAM_IDS" })
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
