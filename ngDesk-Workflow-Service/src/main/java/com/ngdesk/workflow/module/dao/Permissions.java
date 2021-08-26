package com.ngdesk.workflow.module.dao;

import java.util.List;

import javax.validation.constraints.NotNull;

import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Permissions {

	@JsonProperty("CHAT")
	@Field("CHAT")
	@NotNull(message = "TEAMS_REQUIRED_CHAT")
	List<String> chatTeams;

	public Permissions() {

	}

	public Permissions(@NotNull(message = "TEAMS_REQUIRED_CHAT") List<String> chatTeams) {
		super();
		this.chatTeams = chatTeams;
	}

	public List<String> getChatTeams() {
		return chatTeams;
	}

	public void setChatTeams(List<String> chatTeams) {
		this.chatTeams = chatTeams;
	}

}
