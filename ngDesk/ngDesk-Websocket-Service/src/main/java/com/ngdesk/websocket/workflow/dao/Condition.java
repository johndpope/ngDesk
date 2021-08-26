package com.ngdesk.websocket.workflow.dao;

import javax.validation.constraints.Pattern;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Condition {

	@JsonProperty("REQUIREMENT_TYPE")
	private String requirementType;

	@JsonProperty("CONDITION")
	private String condition;

	@JsonProperty("OPERATOR")
	private String operator;

	@JsonProperty("CONDITION_VALUE")
	private String value;

	public Condition() {

	}

	public Condition(String requirementType, String condition, String operator, String value) {
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
