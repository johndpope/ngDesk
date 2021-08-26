package com.ngdesk.module.slas.dao;

import com.ngdesk.commons.annotations.CustomNotEmpty;

import io.swagger.v3.oas.annotations.media.Schema;

public class Violation {

	@Schema(description = "Operator", required = true)
	@CustomNotEmpty(message = "DAO_VARIABLE_REQUIRED", values = "OPERATOR")
	private String operator;

	@Schema(description = "Condition", required = true)
	@CustomNotEmpty(message = "DAO_VARIABLE_REQUIRED", values = "CONDITION")
	private String condition;

	@Schema(description = "Condition Value", required = false)
	private String conditionValue;

	public Violation() {

	}

	public Violation(String operator, String condition, String conditionValue) {
		super();
		this.operator = operator;
		this.condition = condition;
		this.conditionValue = conditionValue;
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
