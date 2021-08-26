package com.ngdesk.tesseract.module.dao;

import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Violation {
	@JsonProperty("OPERATOR")
	@Field("OPERATOR")
	private String opearator;

	@JsonProperty("CONDITION")
	@Field("CONDITION")
	private String condition;

	@JsonProperty("CONDITION_VALUE")
	@Field("CONDITION_VALUE")
	private String conditionValue;

	@JsonProperty("SLA_EXPIRY")
	@Field("SLA_EXPIRY")
	private int slaExpiry;

	public Violation() {

	}

	public Violation(String opearator, String condition, String conditionValue, int slaExpiry) {
		this.opearator = opearator;
		this.condition = condition;
		this.conditionValue = conditionValue;
		this.slaExpiry = slaExpiry;
	}

	public String getOpearator() {
		return opearator;
	}

	public void setOpearator(String opearator) {
		this.opearator = opearator;
	}

	public String getCondition() {
		return condition;
	}

	public void setCondition(String condition) {
		this.condition = condition;
	}

	public String getConditionValue() {
		return conditionValue;
	}

	public void setConditionValue(String conditionValue) {
		this.conditionValue = conditionValue;
	}

	public int getSlaExpiry() {
		return slaExpiry;
	}

	public void setSlaExpiry(int slaExpiry) {
		this.slaExpiry = slaExpiry;
	}

}
