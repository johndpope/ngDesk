package com.ngdesk.role.layout.dao;

import javax.validation.constraints.Pattern;

import com.ngdesk.commons.annotations.CustomNotEmpty;

import io.swagger.v3.oas.annotations.media.Schema;

public class Condition {

	@Schema(description = "Requirement type for the conditions(All or Any)", required = true)
	@CustomNotEmpty(message = "DAO_VARIABLE_REQUIRED", values = { "CONDITION_REQUIREMENT_TYPE" })
	@Pattern(regexp = "Any|All", message = "NOT_VALID_REQUIREMENT_TYPE")
	private String requirementType;

	@Pattern(regexp = "GREATER_THAN|LESS_THAN|EQUALS_TO|NOT_EQUALS_TO|REGEX|DOES_NOT_CONTAIN|CONTAINS|EXISTS|DOES_NOT_EXIST|LENGTH_IS_GREATER_THAN|LENGTH_IS_LESS_THAN", message = "INVALID_CONDITION_OPERATOR")
	private String operator;

	@CustomNotEmpty(message = "DAO_VARIABLE_REQUIRED", values = { "CONDITION_FIELD" })
	private String condition;

	private String conditionValue;

	public Condition(@Pattern(regexp = "Any|All", message = "NOT_VALID_REQUIREMENT_TYPE") String requirementType,
			@Pattern(regexp = "GREATER_THAN|LESS_THAN|EQUALS_TO|NOT_EQUALS_TO|REGEX|DOES_NOT_CONTAIN|CONTAINS|EXISTS|DOES_NOT_EXIST|LENGTH_IS_GREATER_THAN|LENGTH_IS_LESS_THAN", message = "INVALID_CONDITION_OPERATOR") String operator,
			String condition, String conditionValue) {
		super();
		this.requirementType = requirementType;
		this.operator = operator;
		this.condition = condition;
		this.conditionValue = conditionValue;
	}

	public Condition() {
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
