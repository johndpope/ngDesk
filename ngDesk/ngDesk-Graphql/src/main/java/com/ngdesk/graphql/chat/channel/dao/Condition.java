package com.ngdesk.graphql.chat.channel.dao;

import javax.validation.constraints.Pattern;

import org.springframework.data.mongodb.core.mapping.Field;

public class Condition {

	@Field("REQUIREMENT_TYPE")
	private String requirementType;

	@Field("CONDITION")
	private String condition;

	@Field("OPERATOR")
	private String operator;

	@Field("CONDITION_VALUE")
	private String conditionValue;

	public Condition() {

	}

	public Condition(String requirementType, String condition, String operator, String conditionValue) {
		super();
		this.requirementType = requirementType;
		this.condition = condition;
		this.operator = operator;
		this.conditionValue = conditionValue;
	}

	public String getRequirementType() {
		return requirementType;
	}

	public void setRequirementType(String requirementType) {
		this.requirementType = requirementType;
	}

	public String getCondition() {
		return condition;
	}

	public void setCondition(String condition) {
		this.condition = condition;
	}

	public String getOperator() {
		return operator;
	}

	public void setOperator(String operator) {
		this.operator = operator;
	}

	public String getConditionValue() {
		return conditionValue;
	}

	public void setConditionValue(String conditionValue) {
		this.conditionValue = conditionValue;
	}

}
