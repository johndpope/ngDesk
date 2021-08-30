package com.ngdesk.company.settings.dao;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;

public class ChatSettings {

	@Field("TEAMS_WHO_CAN_CHAT")
	@Schema(description = "Teams with chat access", required = false, example = "team Id")
	@JsonProperty("TEAMS_WHO_CAN_CHAT")
	private List<String> teamsWhoCanChat;

	@Field("MAX_CHATS_PER_AGENT")
	@Schema(description = "Max agents per chat", required = false, example = "1")
	@JsonProperty("MAX_CHATS_PER_AGENT")
	private int maxChatsPerAgent = 5;

	@Field("CHAT_BUSINESS_RULES")
	@Schema(description = "Chat business rules", required = false)
	@JsonProperty("CHAT_BUSINESS_RULES")
	private ChatBusinessRules chatBusinessRules;

	public ChatSettings() {

	}

	public ChatSettings(List<String> teamsWhoCanChat, int maxChatsPerAgent, ChatBusinessRules chatBusinessRules) {
		super();
		this.teamsWhoCanChat = teamsWhoCanChat;
		this.maxChatsPerAgent = maxChatsPerAgent;
		this.chatBusinessRules = chatBusinessRules;
	}

	public List<String> getTeamsWhoCanChat() {
		return teamsWhoCanChat;
	}

	public void setTeamsWhoCanChat(List<String> teamsWhoCanChat) {
		this.teamsWhoCanChat = teamsWhoCanChat;
	}

	public int getMaxChatsPerAgent() {
		return maxChatsPerAgent;
	}

	public void setMaxChatsPerAgent(int maxChatsPerAgent) {
		this.maxChatsPerAgent = maxChatsPerAgent;
	}

	public ChatBusinessRules getChatBusinessRules() {
		return chatBusinessRules;
	}

	public void setChatBusinessRules(ChatBusinessRules chatBusinessRules) {
		this.chatBusinessRules = chatBusinessRules;
	}

}
