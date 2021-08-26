package com.ngdesk.tesseract.module.dao;

import java.util.List;

import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Sla {

	@JsonProperty("NAME")
	@Field("NAME")
	private String name;

	@JsonProperty("CONDITIONS")
	@Field("CONDITIONS")
	private List<Condition> conditions;

	@JsonProperty("SLA_ID")
	@Field("SLA_ID")
	private String slaId;

	@JsonProperty("VIOLATIONS")
	@Field("VIOLATIONS")
	private List<Violation> violations;

	@JsonProperty("BUSINESS_RULES")
	@Field("BUSINESS_RULES")
	private SlaBuisnessRules slaBuisinessRules;

	@JsonProperty("DELETED")
	@Field("DELETED")
	private boolean deleted;

	public Sla() {

	}

	public Sla(String name, List<Condition> conditions, String slaId, List<Violation> violations,
			SlaBuisnessRules slaBuisinessRules, boolean deleted) {
		this.name = name;
		this.conditions = conditions;
		this.slaId = slaId;
		this.violations = violations;
		this.slaBuisinessRules = slaBuisinessRules;
		this.deleted = deleted;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<Condition> getConditions() {
		return conditions;
	}

	public void setConditions(List<Condition> conditions) {
		this.conditions = conditions;
	}

	public String getSlaId() {
		return slaId;
	}

	public void setSlaId(String slaId) {
		this.slaId = slaId;
	}

	public List<Violation> getViolations() {
		return violations;
	}

	public void setViolations(List<Violation> violations) {
		this.violations = violations;
	}

	public boolean isDeleted() {
		return deleted;
	}

	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}

	public SlaBuisnessRules getSlaBuisinessRules() {
		return slaBuisinessRules;
	}

	public void setSlaBuisinessRules(SlaBuisnessRules slaBuisinessRules) {
		this.slaBuisinessRules = slaBuisinessRules;
	}

}
