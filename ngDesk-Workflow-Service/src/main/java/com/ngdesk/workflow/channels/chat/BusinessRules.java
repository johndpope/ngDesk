package com.ngdesk.workflow.channels.chat;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;

public class BusinessRules {

	@JsonProperty("TIMEZONE")
	@Field("TIMEZONE")
	private String timezone;

	@JsonProperty("ACTIVE")
	@Field("ACTIVE")
	private boolean active;

	@JsonProperty("RESTRICTION_TYPE")
	@Field("RESTRICTION_TYPE")
	private String restrictionType;

	@JsonProperty("RESTRICTIONS")
	@Field("RESTRICTIONS")
	private List<Restriction> restrictions;

	public BusinessRules() {
	}

	public BusinessRules(String timezone, boolean active, String restrictionType, List<Restriction> restrictions) {
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

	public List<Restriction> getRestrictions() {
		return restrictions;
	}

	public void setRestrictions(List<Restriction> restrictions) {
		this.restrictions = restrictions;
	}

}
