package com.ngdesk.module.channels.chat;

import javax.validation.constraints.NotNull;

import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.validation.Valid;

public class ChatChannelSettings {

	@JsonProperty("BUSINESS_RULES")
	@Field("BUSINESS_RULES")
	@NotNull(message = "BUSINESS_RULES_NOT_NULL")
	@Valid
	private BusinessRules businessRules;

	@JsonProperty("ENABLE_FAQS")
	@Field("ENABLE_FAQS")
	@NotNull(message = "ENABLE_FAQS_NOT_NULL")
	private boolean enable;

	@JsonProperty("PRE_SURVEY_REQUIRED")
	@Field("PRE_SURVEY_REQUIRED")
	@NotNull(message = "PRE_SURVEY_REQUIRED_NOT_NULL")
	private boolean preSurveyRequired;

	@JsonProperty("BOT_SETTINGS")
	@Field("BOT_SETTINGS")
	@Valid
	private BotSettings botSettings;

	public ChatChannelSettings() {
	}

	public ChatChannelSettings(@NotNull(message = "BUSINESS_RULES_NOT_NULL") @Valid BusinessRules businessRules,
			@NotNull(message = "ENABLE_FAQS_NOT_NULL") boolean enable,
			@NotNull(message = "PRE_SURVEY_REQUIRED_NOT_NULL") boolean preSurveyRequired, BotSettings botSettings) {
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
