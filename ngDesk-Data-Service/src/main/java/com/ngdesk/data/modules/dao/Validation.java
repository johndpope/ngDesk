package com.ngdesk.data.modules.dao;

import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Validation {
	@JsonProperty("REQUIREMENT_TYPE")
	@Field("REQUIREMENT_TYPE")
	private String requirementType;

	@JsonProperty("OPERATOR")
	@Field("OPERATOR")
	private String operator;

	@JsonProperty("CONDITION")
	@Field("CONDITION")
	private String condition;

	@JsonProperty("CONDITION_VALUE")
	@Field("CONDITION_VALUE")
	private String conditionValue;

	public Validation() {
	}

	
	public Validation(String requirementType, String operator, String condition, String conditionValue) {
		super();
		this.requirementType = requirementType;
		this.operator = operator;
		this.condition = condition;
		this.conditionValue = conditionValue;
	}


	public String getRequirementType() {
		return requirementType;
	}

	public void setRequirementType(String requirementType) {
		this.requirementType = requirementType;
	}

	public String getOperator() {
		return operator;
	}

	public void setOperator(String operator) {
		this.operator = operator;
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
