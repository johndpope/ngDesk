package com.ngdesk.commons.models;

import javax.validation.constraints.Pattern;

import com.ngdesk.commons.annotations.CustomNotEmpty;

public class DashboardCondition {

	@CustomNotEmpty(message = "DAO_VARIABLE_REQUIRED", values = { "CONDITION" })
	private String condition;

	@CustomNotEmpty(message = "DAO_VARIABLE_REQUIRED", values = { "OPERATOR" })
	@Pattern(regexp = "CHANGED|EQUALS_TO|NOT_EQUALS_TO|CONTAINS|DOES_NOT_CONTAIN|REGEX|LESS_THAN|GREATER_THAN|LENGTH_GREATER_THAN|LENGTH_LESS_THAN", message = "NOT_VALID_OPERATOR")
	private String operator;

	@CustomNotEmpty(message = "DAO_VARIABLE_REQUIRED", values = { "VALUE" })
	private String value;

	@CustomNotEmpty(message = "DAO_VARIABLE_REQUIRED", values = { "REQUIREMENT_TYPE" })

	@Pattern(regexp = "All|Any", message = "INVALID_REQUIREMENT_TYPE")
	private String requirementType;

	public DashboardCondition() {

	}

	public DashboardCondition(String condition,
			@Pattern(regexp = "CHANGED|EQUALS_TO|NOT_EQUALS_TO|CONTAINS|DOES_NOT_CONTAIN|REGEX|LESS_THAN|GREATER_THAN|LENGTH_GREATER_THAN|LENGTH_LESS_THAN", message = "NOT_VALID_OPERATOR") String operator,
			String value, @Pattern(regexp = "All|Any", message = "INVALID_REQUIREMENT_TYPE") String requirementType) {
		super();
		this.condition = condition;
		this.operator = operator;
		this.value = value;
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

	public String getRequirementType() {
		return requirementType;
	}

	public void setRequirementType(String requirementType) {
		this.requirementType = requirementType;
	}

}
