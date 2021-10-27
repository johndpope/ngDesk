package com.ngdesk.graphql.modules.dao;

import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Condition {

	@Field("REQUIREMENT_TYPE")
	private String requirementType;

	@Field("OPERATOR")
	private String operator;

	@Field("CONDITION")
	private String condition;

	@Field("CONDITION_VALUE")
	private String conditionValue;

	public Condition() {
	}

	public Condition(String requirementType, String operator, String condition, String conditionValue) {
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

	public void setOpearator(String operator) {
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
