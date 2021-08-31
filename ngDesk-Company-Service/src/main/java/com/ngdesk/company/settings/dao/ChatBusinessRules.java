package com.ngdesk.company.settings.dao;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.Pattern;

import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;

public class ChatBusinessRules {

	@Field("RESTRICTION_TYPE")
	@Schema(description = "Restriction type", required = false, example = "Daily")
	@JsonProperty("RESTRICTION_TYPE")
	@Pattern(regexp = "Day|Week", message = "INVALID_RESTRICTION_TYPE")
	private String restrictionType;

	@Field("CHAT_RESTRICTIONS")
	@Schema(description = "Chat restrictions", required = false)
	@JsonProperty("CHAT_RESTRICTIONS")
	@Valid
	private List<ChatRestrictions> chatRestrictions;

	public ChatBusinessRules() {

	}

	public ChatBusinessRules(@Pattern(regexp = "Day|Week", message = "INVALID_RESTRICTION_TYPE") String restrictionType,
			@Valid List<ChatRestrictions> chatRestrictions) {
		super();
		this.restrictionType = restrictionType;
		this.chatRestrictions = chatRestrictions;
	}

	public String getRestrictionType() {
		return restrictionType;
	}

	public void setRestrictionType(String restrictionType) {
		this.restrictionType = restrictionType;
	}

	public List<ChatRestrictions> getChatRestrictions() {
		return chatRestrictions;
	}

	public void setChatRestrictions(List<ChatRestrictions> chatRestrictions) {
		this.chatRestrictions = chatRestrictions;
	}

}
