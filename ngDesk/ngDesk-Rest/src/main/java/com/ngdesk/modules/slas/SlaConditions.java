package com.ngdesk.modules.slas;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import javax.validation.constraints.Pattern.Flag;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ngdesk.annotations.ValidOperator;
import com.ngdesk.annotations.ValidSlaOperators;

import io.swagger.annotations.ApiModelProperty;

public class SlaConditions {
	@JsonProperty("REQUIREMENT_TYPE")
	@NotNull(message = "REQUIREMENT_TYPE_NOT_NULL")
	@Pattern(regexp = "Any|All", flags = { Pattern.Flag.CASE_INSENSITIVE }, message = "INVALID_REQUIREMENT_TYPE")
	@Size(min = 1, message = "REQUIREMENT_TYPE_NOT_EMPTY")
	private String requirementType;

	@JsonProperty("OPERATOR")
	@NotNull(message = "OPERATOR_NOT_NULL")
	@Size(min = 1, message = "OPERATOR_NOT_EMPTY")
	@ApiModelProperty(notes = "Valid Operators: equals to, not equals to, contains, does not contain, regex, is unique")
	@ValidSlaOperators
	private String opearator;

	@JsonProperty("CONDITION")
	@NotNull(message = "CONDITION_NOT_NULL")
	private String condition;

	@JsonProperty("CONDITION_VALUE")
	private String conditionValue;

	public SlaConditions() {
	}

	public SlaConditions(
			@NotNull(message = "REQUIREMENT_TYPE_NOT_NULL") @Pattern(regexp = "Any|All", flags = Flag.CASE_INSENSITIVE, message = "INVALID_REQUIREMENT_TYPE") @Size(min = 1, message = "REQUIREMENT_TYPE_NOT_EMPTY") String requirementType,
			@NotNull(message = "OPERATOR_NOT_NULL") @Size(min = 1, message = "OPERATOR_NOT_EMPTY") String opearator,
			@NotNull(message = "CONDITION_NOT_NULL") @Valid String condition, String conditionValue) {
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
