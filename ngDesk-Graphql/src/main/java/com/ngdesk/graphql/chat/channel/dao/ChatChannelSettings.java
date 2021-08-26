package com.ngdesk.graphql.chat.channel.dao;

import org.springframework.data.mongodb.core.mapping.Field;

public class ChatChannelSettings {

	@Field("BUSINESS_RULES")
	private BusinessRules businessRules;

	@Field("ENABLE_FAQS")
	private boolean enable;

	@Field("PRE_SURVEY_REQUIRED")
	private boolean preSurveyRequired;

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
