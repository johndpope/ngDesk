package com.ngdesk.module.slas.dao;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Pattern.Flag;

import com.ngdesk.commons.annotations.CustomNotEmpty;

import io.swagger.v3.oas.annotations.media.Schema;

public class SLAConditions {

	@Schema(description = "Reqiurement type", required = false)
	@Pattern(regexp = "Any|All", flags = { Pattern.Flag.CASE_INSENSITIVE }, message = "INVALID_REQUIREMENT_TYPE")
	private String requirementType;

	
	@Schema(description = "Operator", required = false)
	@CustomNotEmpty(message = "DAO_VARIABLE_REQUIRED", values = "OPERATOR")
	private String operator;

	@Schema(description = "Condition", required = false)
	@CustomNotEmpty(message = "DAO_VARIABLE_REQUIRED", values = "CONDITION")
	private String condition;

	@Schema(description = "Condition Value", required = false)
	private String conditionValue;

	public SLAConditions() {

	}

	public SLAConditions(
			@Pattern(regexp = "Any|All", flags = Flag.CASE_INSENSITIVE, message = "INVALID_REQUIREMENT_TYPE") String requirementType,
			String operator, String condition, String conditionValue) {
		super();
		this.requirementType = requirementType;
		this.operator = operator;
		this.condition = condition;
		this.conditionValue = conditionValue;
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
