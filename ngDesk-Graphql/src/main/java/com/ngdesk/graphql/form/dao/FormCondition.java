package com.ngdesk.graphql.form.dao;

import javax.validation.constraints.Pattern;

import com.ngdesk.commons.annotations.CustomNotEmpty;

public class FormCondition {

	private String requirementType;

	private String opearator;

	private String condition;

	private String conditionValue;

	public FormCondition() {

	}

	public FormCondition(
			@Pattern(regexp = "fill|standard|outline", message = "INVALID_REQUIREMENT_TYPE") String requirementType,
			@Pattern(regexp = "CHANGED|EQUALS_TO|NOT_EQUALS_TO|CONTAINS|DOES_NOT_CONTAIN|REGEX|LESS_THAN|GREATER_THAN|LENGTH_LESS_THAN|LENGTH_GREATER_THAN|IS_EQUALS", message = "INVALID_OPERATOR") String opearator,
			String condition, String conditionValue) {
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
