package com.ngdesk.websocket.channels.chat.dao;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Conditions {

	@JsonProperty("REQUIREMENT_TYPE")
	@Field("REQUIREMENT_TYPE")
	private String requirementType;

	@JsonProperty("OPERATOR")
	@Field("OPERATOR")
	private String opearator;

	@JsonProperty("CONDITION")
	@Field("CONDITION")
	private String condition;

	@JsonProperty("CONDITION_VALUE")
	@Field("CONDITION_VALUE")
	private String conditionValue;

	public Conditions() {

	}

	public Conditions(String requirementType, String opearator, String condition, String conditionValue) {
		super();
		this.requirementType = requirementType;
		this.opearator = opearator;
		this.condition = condition;
		this.conditionValue = conditionValue;
	}

	public String getRequirementType() {
		return requirementType;
	}

	public void setRequirementType(String requirementType) {
		this.requirementType = requirementType;
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

}
