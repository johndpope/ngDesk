package com.ngdesk.module.channels.chat;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Conditions {

	@JsonProperty("REQUIREMENT_TYPE")
	@Field("REQUIREMENT_TYPE")
	@NotNull(message = "REQUIREMENT_TYPE_NOT_NULL")
	@Pattern(regexp = "Any|All", message = "INVALID_REQUIREMENT_TYPE")
	@Size(min = 1, message = "REQUIREMENT_TYPE_NOT_EMPTY")
	@Valid
	private String requirementType;

	@JsonProperty("OPERATOR")
	@Field("OPERATOR")
	@Pattern(regexp = "EQUALS_TO|NOT_EQUALS_TO|GREATER_THAN|LESS_THAN|REGEX|DOES_NOT_CONTAIN|CONTAINS", message = "INVALID_OPERATOR")
	private String opearator;

	@JsonProperty("CONDITION")
	@Field("CONDITION")
	@NotNull(message = "CONDITION_NOT_NULL")
	private String condition;

	@JsonProperty("CONDITION_VALUE")
	@Field("CONDITION_VALUE")
	@NotNull(message = "CONDITION_NOT_NULL")
	private String conditionValue;

	public Conditions() {

	}

	public Conditions(
			@NotNull(message = "REQUIREMENT_TYPE_NOT_NULL") @Pattern(regexp = "Any|All", message = "INVALID_REQUIREMENT_TYPE") @Size(min = 1, message = "REQUIREMENT_TYPE_NOT_EMPTY") @Valid String requirementType,
			@Pattern(regexp = "EQUALS_TO|NOT_EQUALS_TO|GREATER_THAN|LESS_THAN|REGEX|DOES_NOT_CONTAIN|CONTAINS", message = "INVALID_OPERATOR") String opearator,
			@NotNull(message = "CONDITION_NOT_NULL") String condition,
			@NotNull(message = "CONDITION_NOT_NULL") String conditionValue) {
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
