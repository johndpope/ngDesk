package com.ngdesk.graphql.escalation.dao;

import java.util.List;

import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;

public class EscalateTo {

	@JsonProperty("SCHEDULE_IDS")
	@Field("SCHEDULE_IDS")
	private List<String> scheduleIds;

	@JsonProperty("USER_IDS")
	@Field("USER_IDS")
	private List<String> userIds;

	@JsonProperty("TEAM_IDS")
	@Field("TEAM_IDS")
	private List<String> teamIds;

	public EscalateTo() {

	}

	public EscalateTo(List<String> scheduleIds, List<String> userIds, List<String> teamIds) {
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
