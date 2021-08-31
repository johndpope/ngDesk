package com.ngdesk.graphql.company.dao;

import java.util.List;

import org.springframework.data.mongodb.core.mapping.Field;

import com.ngdesk.graphql.chat.channel.dao.ChatRestriction;

public class ChatBusinessRules {

	@Field("RESTRICTION_TYPE")
	private String restrictionType;

	@Field("CHAT_RESTRICTIONS")
	private List<ChatRestriction> chatRestrictions;

	public ChatBusinessRules() {

	}

	public ChatBusinessRules(String restrictionType, List<ChatRestriction> chatRestrictions) {
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

	public List<ChatRestriction> getChatRestrictions() {
		return chatRestrictions;
	}

	public void setChatRestrictions(List<ChatRestriction> chatRestrictions) {
		this.chatRestrictions = chatRestrictions;
	}

}
