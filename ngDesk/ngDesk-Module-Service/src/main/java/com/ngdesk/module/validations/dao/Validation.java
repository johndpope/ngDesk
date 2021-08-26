package com.ngdesk.module.validations.dao;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Pattern.Flag;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ngdesk.commons.annotations.CustomNotEmpty;

public class Validation {
	@JsonProperty("REQUIREMENT_TYPE")
	@CustomNotEmpty(message = "DAO_VARIABLE_REQUIRED", values = "REQUIREMENT_TYPE")
	@Pattern(regexp = "ANY|ALL", flags = { Pattern.Flag.CASE_INSENSITIVE }, message = "INVALID_REQUIREMENT_TYPE")
	private String requirementType;

	@JsonProperty("OPERATOR")
	@CustomNotEmpty(message = "DAO_VARIABLE_REQUIRED", values = "OPERATOR")
	private String operator;

	@JsonProperty("CONDITION")
	@CustomNotEmpty(message = "DAO_VARIABLE_REQUIRED", values = "CONDITION")
	private String condition;

	@JsonProperty("CONDITION_VALUE")
	private String conditionValue;

	public Validation() {
	}

	public Validation(
			@Pattern(regexp = "ANY|ALL", flags = Flag.CASE_INSENSITIVE, message = "INVALID_REQUIREMENT_TYPE") String requirementType,
			String operator, String condition, String conditionValue) {
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
