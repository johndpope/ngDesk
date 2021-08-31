package com.ngdesk.graphql.company.dao;

import java.util.List;

import org.springframework.data.mongodb.core.mapping.Field;

public class ChatSettings {

	@Field("TEAMS_WHO_CAN_CHAT")
	private List<String> teamsWhoCanChat;

	@Field("HAS_RESTRICTIONS")
	private Boolean hasRestrictions;

	@Field("CHAT_BUSINESS_RULES")
	private ChatBusinessRules chatBusinessRules;

	public ChatSettings() {

	}

	public ChatSettings(List<String> teamsWhoCanChat, Boolean hasRestrictions, ChatBusinessRules chatBusinessRules) {
		super();
		this.teamsWhoCanChat = teamsWhoCanChat;
		this.hasRestrictions = hasRestrictions;
		this.chatBusinessRules = chatBusinessRules;
	}

	public List<String> getTeamsWhoCanChat() {
		return teamsWhoCanChat;
	}

	public void setTeamsWhoCanChat(List<String> teamsWhoCanChat) {
		this.teamsWhoCanChat = teamsWhoCanChat;
	}

	public Boolean getHasRestrictions() {
		return hasRestrictions;
	}

	public void setHasRestrictions(Boolean hasRestrictions) {
		this.hasRestrictions = hasRestrictions;
	}

	public ChatBusinessRules getChatBusinessRules() {
		return chatBusinessRules;
	}

	public void setChatBusinessRules(ChatBusinessRules chatBusinessRules) {
		this.chatBusinessRules = chatBusinessRules;
	}

}
