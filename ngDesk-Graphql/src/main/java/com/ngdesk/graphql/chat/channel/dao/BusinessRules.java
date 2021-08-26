package com.ngdesk.graphql.chat.channel.dao;

import java.util.List;

import org.springframework.data.mongodb.core.mapping.Field;

public class BusinessRules {

	@Field("TIMEZONE")
	private String timezone;

	@Field("ACTIVE")
	private boolean active;

	@Field("RESTRICTION_TYPE")
	private String restrictionType;

	@Field("RESTRICTIONS")
	private List<ChatRestriction> restrictions;

	public BusinessRules() {
	}

	public BusinessRules(String timezone, boolean active, String restrictionType, List<ChatRestriction> restrictions) {
		super();
		this.timezone = timezone;
		this.active = active;
		this.restrictionType = restrictionType;
		this.restrictions = restrictions;
	}

	public String getTimezone() {
		return timezone;
	}

	public void setTimezone(String timezone) {
		this.timezone = timezone;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public String getRestrictionType() {
		return restrictionType;
	}

	public void setRestrictionType(String restrictionType) {
		this.restrictionType = restrictionType;
	}

	public List<ChatRestriction> getRestrictions() {
		return restrictions;
	}

	public void setRestrictions(List<ChatRestriction> restrictions) {
		this.restrictions = restrictions;
	}

}
