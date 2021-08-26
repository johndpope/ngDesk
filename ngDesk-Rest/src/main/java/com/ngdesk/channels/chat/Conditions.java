package com.ngdesk.channels.chat;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ngdesk.annotations.ValidChatPromptConditions;
import io.swagger.annotations.ApiModelProperty;

public class Conditions {
	
	@JsonProperty("REQUIREMENT_TYPE")
	@NotNull(message = "REQUIREMENT_TYPE_NOT_NULL")
	@Pattern(regexp = "Any|All", message = "INVALID_REQUIREMENT_TYPE")
	@Size(min = 1, message = "REQUIREMENT_TYPE_NOT_EMPTY")
	@Valid
	private String requirementType;

	@JsonProperty("OPERATOR")
	@ApiModelProperty(notes = "Valid Operators: equals to, not equals to, lesser than, greater than")
	private String opearator;

	@JsonProperty("CONDITION")
	@NotNull(message = "CONDITION_NOT_NULL")
	@ValidChatPromptConditions
	private String condition;

	@JsonProperty("CONDITION_VALUE")
	private String conditionValue;

	public Conditions() {
		
	}

	public Conditions(
			@NotNull(message = "REQUIREMENT_TYPE_NOT_NULL") @Pattern(regexp = "Any|All", message = "INVALID_REQUIREMENT_TYPE") @Size(min = 1, message = "REQUIREMENT_TYPE_NOT_EMPTY") String requirementType,
			@NotNull(message = "OPERATOR_NOT_NULL") @Size(min = 1, message = "OPERATOR_NOT_EMPTY") String opearator,
			@NotNull(message = "CONDITION_NOT_NULL") String condition, String conditionValue) {
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
