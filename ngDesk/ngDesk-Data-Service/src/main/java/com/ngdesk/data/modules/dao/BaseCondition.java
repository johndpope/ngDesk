package com.ngdesk.data.modules.dao;

import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;

public class BaseCondition {

	@JsonProperty("CONDITION")
	@Field("CONDITION")
	private String condition;

	@JsonProperty("OPERATOR")
	@Field("OPERATOR")
	private String opearator;

	@JsonProperty("VALUE")
	@Field("VALUE")
	private String value;

	public BaseCondition() {

	}

	public BaseCondition(String condition, String opearator, String value) {
		super();
		this.condition = condition;
		this.opearator = opearator;
		this.value = value;
	}

	public String getCondition() {
		return condition;
	}

	public String getOpearator() {
		return opearator;
	}

	public String getValue() {
		return value;
	}

	public void setCondition(String condition) {
		this.condition = condition;
	}

	public void setOpearator(String opearator) {
		this.opearator = opearator;
	}

	public void setValue(String value) {
		this.value = value;
	}

}
