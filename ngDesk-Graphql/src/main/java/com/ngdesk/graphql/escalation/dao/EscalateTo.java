package com.ngdesk.graphql.escalation.dao;

import java.util.List;

import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;

public class EscalateTo {

	private List<String> schedules;
	private List<String> users;
	private List<String> teams;

	public EscalateTo() {

	}

	public EscalateTo(List<String> schedules, List<String> users, List<String> teams) {
		super();
		this.schedules = schedules;
		this.users = users;
		this.teams = teams;
	}

	public List<String> getSchedules() {
		return schedules;
	}

	public void setSchedules(List<String> schedules) {
		this.schedules = schedules;
	}

	public List<String> getUsers() {
		return users;
	}

	public void setUsers(List<String> users) {
		this.users = users;
	}

	public List<String> getTeams() {
		return teams;
	}

	public void setTeams(List<String> teams) {
		this.teams = teams;
	}

}
