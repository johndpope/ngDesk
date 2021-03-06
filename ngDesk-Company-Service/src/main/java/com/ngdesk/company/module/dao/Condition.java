package com.ngdesk.company.module.dao;

import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Condition {
	
	@Field("REQUIREMENT_TYPE")
	@JsonProperty("REQUIREMENT_TYPE")
	private String requirementType;

	@Field("OPERATOR")
	@JsonProperty("OPERATOR")
	private String opearator;

	@Field("CONDITION")
	@JsonProperty("CONDITION")
	private String condition;

	@Field("CONDITION_VALUE")
	@JsonProperty("CONDITION_VALUE")
	private String conditionValue;

	public Condition() {
	}

	public Condition(String requirementType, String opearator, String condition, String conditionValue) {
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
