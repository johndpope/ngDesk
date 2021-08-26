package com.ngdesk.graphql.slas.dao;

public class Violation {

	private String operator;

	private String condition;

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
