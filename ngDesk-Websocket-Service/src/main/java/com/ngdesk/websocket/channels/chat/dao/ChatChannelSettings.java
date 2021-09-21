package com.ngdesk.websocket.channels.chat.dao;

import javax.validation.constraints.NotNull;

import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.validation.Valid;

public class ChatChannelSettings {

	@JsonProperty("BUSINESS_RULES")
	@Field("BUSINESS_RULES")
	private BusinessRules businessRules;

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

	public ChatChannelSettings(BusinessRules businessRules, boolean enable, boolean preSurveyRequired,
			BotSettings botSettings) {
		super();
		this.businessRules = businessRules;
		this.enable = enable;
		this.preSurveyRequired = preSurveyRequired;
		this.botSettings = botSettings;
	}

	public BusinessRules getBusinessRules() {
		return businessRules;
	}

	public void setBusinessRules(BusinessRules businessRules) {
		this.businessRules = businessRules;
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
