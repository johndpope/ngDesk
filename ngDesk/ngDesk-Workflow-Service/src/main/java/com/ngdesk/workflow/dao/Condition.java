package com.ngdesk.workflow.dao;

import javax.validation.constraints.Pattern;

import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ngdesk.commons.annotations.CustomNotEmpty;

import io.swagger.v3.oas.annotations.media.Schema;

public class Condition {

	@Schema(description = "Name of the stage, needs to be unique", required = true, example = "All")
	@JsonProperty("REQUIREMENT_TYPE")
	@Field("REQUIREMENT_TYPE")
	@CustomNotEmpty(message = "DAO_VARIABLE_REQUIRED", values = { "Condition", "requirement type" })
	@Pattern(regexp = "All|Any", message = "NOT_VALID_REQUIREMENT_TYPE")
	private String requirementType;

	@Schema(description = "Field id of a field in the module", required = true, example = "0113c2ee-ec6f-4b02-b9d9-a30fdec09b4a")
	@JsonProperty("CONDITION")
	@Field("CONDITION")
	@CustomNotEmpty(message = "DAO_VARIABLE_REQUIRED", values = { "CONDITION" })
	private String condition;

	@Schema(description = "Operator for field", required = true, example = "EQUALS_TO")
	@JsonProperty("OPERATOR")
	@Field("OPERATOR")
	@CustomNotEmpty(message = "DAO_VARIABLE_REQUIRED", values = { "OPERATOR" })
	@Pattern(regexp = "CHANGED|EQUALS_TO|NOT_EQUALS_TO|CONTAINS|DOES_NOT_CONTAIN|REGEX|LESS_THAN|GREATER_THAN|LENGTH_GREATER_THAN|LENGTH_LESS_THAN|IS_UNIQUE", message = "NOT_VALID_OPERATOR")
	private String operator;

	@Schema(description = "Value for evaluation of condition", required = false, example = "Low")
	@JsonProperty("CONDITION_VALUE")
	@Field("CONDITION_VALUE")
	private String value;

	public Condition() {

	}

	public Condition(@Pattern(regexp = "All|Any", message = "NOT_VALID_REQUIREMENT_TYPE") String requirementType,
			String condition, String operator, String value) {
		super();
		this.requirementType = requirementType;
		this.condition = condition;
		this.operator = operator;
		this.value = value;
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

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

}
