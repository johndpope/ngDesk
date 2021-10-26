package com.ngdesk.escalation.dao;

import java.util.List;

import javax.validation.constraints.NotNull;
import com.ngdesk.commons.annotations.CustomNotNull;
import io.swagger.v3.oas.annotations.media.Schema;

public class EscalateTo {

	@Schema(description = "List of schedule Ids")
	@CustomNotNull(message = "NOT_NULL", values = { "SCHEDULES" })
	private List<String> schedules;
	@Schema(description = "List of user Ids")
	@CustomNotNull(message = "NOT_NULL", values = { "USERS" })
	private List<String> users;

	@Schema(description = "List of team Ids")
	@CustomNotNull(message = "NOT_NULL", values = { "TEAMS" })
	private List<String> teams;

	public EscalateTo() {

	}

	public EscalateTo(@NotNull(message = "SCHEDULES_NOT_NULL") List<String> schedules,
			@NotNull(message = "USERS_NOT_NULL") List<String> users,
			@NotNull(message = "TEAMS_NOT_NULL") List<String> teams) {
		super();
		this.schedules = schedules;
		this.users = users;
		this.teams = teams;
	}

	public List<String> getUsers() {
		return users;
	}

	public void setUsers(List<String> users) {
		this.users = users;
	}

	public List<String> getSchedules() {
		return schedules;
	}

	public void setSchedules(List<String> schedules) {
		this.schedules = schedules;
	}

	public List<String> getTeams() {
		return teams;
	}

	public void setTeams(List<String> teams) {
		this.teams = teams;
	}

}
