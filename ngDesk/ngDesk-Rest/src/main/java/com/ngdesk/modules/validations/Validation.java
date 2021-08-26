package com.ngdesk.modules.validations;

import java.sql.Timestamp;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Pattern.Flag;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ngdesk.annotations.ValidOperator;

public class Validation {
	@JsonProperty("REQUIREMENT_TYPE")
	@NotNull(message = "REQUIREMENT_TYPE_NOT_NULL")
	@Pattern(regexp = "ANY|ALL", flags = { Pattern.Flag.CASE_INSENSITIVE }, message = "INVALID_REQUIREMENT_TYPE")
	@Size(min = 1, message = "REQUIREMENT_TYPE_NOT_EMPTY")
	private String requirementType;

	@JsonProperty("OPERATOR")
	@NotNull(message = "OPERATOR_NOT_NULL")
	@Size(min = 1, message = "OPERATOR_NOT_EMPTY")
	@ValidOperator
	private String operator;

	@JsonProperty("CONDITION")
	@NotNull(message = "CONDITION_NOT_NULL")
	private String condition;

	@JsonProperty("CONDITION_VALUE")
	private String conditionValue;

	public Validation() {
	}

	public Validation(
			@NotNull(message = "REQUIREMENT_TYPE_NOT_NULL") @Pattern(regexp = "ANY|ALL", flags = Flag.CASE_INSENSITIVE, message = "INVALID_REQUIREMENT_TYPE") @Size(min = 1, message = "REQUIREMENT_TYPE_NOT_EMPTY") String requirementType,
			@NotNull(message = "OPERATOR_NOT_NULL") @Size(min = 1, message = "OPERATOR_NOT_EMPTY") String operator,
			@NotNull(message = "CONDITION_NOT_NULL") String condition, String conditionValue) {
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
