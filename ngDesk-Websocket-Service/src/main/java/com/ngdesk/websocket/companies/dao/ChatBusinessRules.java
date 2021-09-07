package com.ngdesk.websocket.companies.dao;

import java.util.List;

import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ChatBusinessRules {

	@Field("RESTRICTION_TYPE")
	@JsonProperty("RESTRICTION_TYPE")
	private String restrictionType;

	@Field("CHAT_RESTRICTIONS")
	@JsonProperty("CHAT_RESTRICTIONS")
	private List<ChatRestrictions> chatRestrictions;

	public ChatBusinessRules() {

	}

	public ChatBusinessRules(String restrictionType, List<ChatRestrictions> chatRestrictions) {
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
