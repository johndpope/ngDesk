package com.ngdesk.tesseract.module.dao;

import java.util.List;

import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SlaBuisnessRules {

	@JsonProperty("HAS_RESTRICTIONS")
	@Field("HAS_RESTRICTIONS")
	private boolean hasRestrictions;

	@JsonProperty("RESTRICTION_TYPE")
	@Field("RESTRICTION_TYPE")
	private String restrictionType;

	@JsonProperty("SLA_RESTRICTIONS")
	@Field("SLA_RESTRICTIONS")
	private List<Restriction> restrictions;

	public SlaBuisnessRules() {
	}

	public SlaBuisnessRules(boolean hasRestrictions, String restrictionType, List<Restriction> restrictions) {
		this.hasRestrictions = hasRestrictions;
		this.restrictionType = restrictionType;
		this.restrictions = restrictions;
	}

	public boolean isHasRestrictions() {
		return hasRestrictions;
	}

	public void setHasRestrictions(boolean hasRestrictions) {
		this.hasRestrictions = hasRestrictions;
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
