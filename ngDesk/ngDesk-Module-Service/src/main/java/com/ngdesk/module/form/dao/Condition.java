package com.ngdesk.module.form.dao;

import javax.validation.constraints.Pattern;

import com.ngdesk.commons.annotations.CustomNotEmpty;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.AccessMode;

public class Condition {

	@Schema(description = "Name of the requirement type, needs to be unique", required = true, example = "All")
	@Pattern(regexp = "All|Any", message = "INVALID_REQUIREMENT_TYPE")
	@CustomNotEmpty(message = "DAO_VARIABLE_REQUIRED", values = { "Condition", "Requirement Type" })
	private String requirementType;

	@CustomNotEmpty(message = "DAO_VARIABLE_REQUIRED", values = { "CONDITION" })
	@Schema(description = "Field id of a field in a module", required = true)
	private String condition;

	@Schema(description = "Operator for Field", required = true, example = "EQUALS_TO")
	@Pattern(regexp = "CHANGED|EQUALS_TO|NOT_EQUALS_TO|CONTAINS|DOES_NOT_CONTAIN|REGEX|LESS_THAN|GREATER_THAN|LENGTH_LESS_THAN|LENGTH_GREATER_THAN|IS_EQUALS", message = "INVALID_OPERATOR")
	@CustomNotEmpty(message = "DAO_VARIABLE_REQUIRED", values = { "OPERATOR" })
	private String opearator;

	@Schema(description = "Value for evaluation of condition", required = false, example = "Low")
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
