package com.ngdesk.graphql.role.layout.dao;

public class RoleLayoutCondition {

	private String requirementType;

	private String operator;

	private String condition;

	private String conditionValue;

	public RoleLayoutCondition(String requirementType, String operator, String condition, String conditionValue) {
		super();
		this.requirementType = requirementType;
		this.operator = operator;
		this.condition = condition;
		this.conditionValue = conditionValue;
	}

	public RoleLayoutCondition() {
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
