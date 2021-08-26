package com.ngdesk.graphql.chat.channel.dao;

import org.springframework.data.mongodb.core.mapping.Field;

public class BotSettings {

	@Field("BOT_ENABLED")
	private boolean enabled;

	@Field("CHAT_BOT")
	private String chatBot;

	public BotSettings() {

	}

	public BotSettings(boolean enabled, String chatBot) {
		super();
		this.enabled = enabled;
		this.chatBot = chatBot;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public String getChatBot() {
		return chatBot;
	}

	public void setChatBot(String chatBot) {
		this.chatBot = chatBot;
	}

}
