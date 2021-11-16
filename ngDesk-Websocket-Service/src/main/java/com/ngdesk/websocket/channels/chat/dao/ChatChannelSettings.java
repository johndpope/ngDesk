package com.ngdesk.websocket.channels.chat.dao;

import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ChatChannelSettings {

	@JsonProperty("ENABLE_FAQS")
	@Field("ENABLE_FAQS")
	private boolean enable;

	@JsonProperty("PRE_SURVEY_REQUIRED")
	@Field("PRE_SURVEY_REQUIRED")
	private boolean preSurveyRequired;

	@JsonProperty("BOT_SETTINGS")
	@Field("BOT_SETTINGS")
	private BotSettings botSettings;

	public ChatChannelSettings() {
	}

	public ChatChannelSettings(boolean enable, boolean preSurveyRequired, BotSettings botSettings) {
		super();
		this.enable = enable;
		this.preSurveyRequired = preSurveyRequired;
		this.botSettings = botSettings;
	}

	public boolean isEnable() {
		return enable;
	}

	public void setEnable(boolean enable) {
		this.enable = enable;
	}

	public boolean isPreSurveyRequired() {
		return preSurveyRequired;
	}

	public void setPreSurveyRequired(boolean preSurveyRequired) {
		this.preSurveyRequired = preSurveyRequired;
	}

	public BotSettings getBotSettings() {
		return botSettings;
	}

	public void setBotSettings(BotSettings botSettings) {
		this.botSettings = botSettings;
	}

}
