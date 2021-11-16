package com.ngdesk.websocket.companies.dao;

import java.util.List;

import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ChatSettings {

	@JsonProperty("MAX_CHATS_PER_AGENT")
	@Field("MAX_CHATS_PER_AGENT")
	private int maxChatPerAgent;

	@Field("TEAMS_WHO_CAN_CHAT")
	@JsonProperty("TEAMS_WHO_CAN_CHAT")
	private List<String> teamsWhoCanChat;

	@Field("HAS_RESTRICTIONS")
	@JsonProperty("HAS_RESTRICTIONS")
	private Boolean hasRestrictions;

	@Field("CHAT_BUSINESS_RULES")
	@JsonProperty("CHAT_BUSINESS_RULES")
	private ChatBusinessRules chatBusinessRules;

	public ChatSettings() {

	}

	public ChatSettings(int maxChatPerAgent, List<String> teamsWhoCanChat, Boolean hasRestrictions,
			ChatBusinessRules chatBusinessRules) {
		super();
		this.maxChatPerAgent = maxChatPerAgent;
		this.teamsWhoCanChat = teamsWhoCanChat;
		this.hasRestrictions = hasRestrictions;
		this.chatBusinessRules = chatBusinessRules;
	}

	public int getMaxChatPerAgent() {
		return maxChatPerAgent;
	}

	public void setMaxChatPerAgent(int maxChatPerAgent) {
		this.maxChatPerAgent = maxChatPerAgent;
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
