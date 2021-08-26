package com.ngdesk.graphql.workflow;

import javax.validation.constraints.Pattern;

import org.springframework.data.mongodb.core.mapping.Field;

public class Condition {

	@Field("REQUIREMENT_TYPE")
	private String requirementType;

	@Field("CONDITION")
	private String condition;

	@Field("OPERATOR")
	private String operator;

	@Field("CONDITION_VALUE")
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
