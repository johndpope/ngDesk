package com.ngdesk.modules.slas;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ngdesk.annotations.ValidOperator;
import com.ngdesk.annotations.ValidSlaOperators;
import com.ngdesk.annotations.ValidViolationOperators;

import io.swagger.annotations.ApiModelProperty;

public class Violation {
	@JsonProperty("OPERATOR")
	@NotNull(message = "OPERATOR_NOT_NULL")
	@Size(min = 1, message = "OPERATOR_NOT_EMPTY")
	@ApiModelProperty(notes = "Valid Operators: has Been, has not changed, has not been replied to by, is past by, is within")
	@ValidViolationOperators
	private String opearator;

	@JsonProperty("CONDITION")
	@NotNull(message = "CONDITION_NOT_NULL")
	private String condition;

	@JsonProperty("CONDITION_VALUE")
	@NotNull(message = "CONDITION_VALUE_NOT_NULL")
	private String conditionValue;

	@JsonProperty("SLA_EXPIRY")
	private int slaExpiry;

	public Violation() {

	}

	public Violation(
			@NotNull(message = "OPERATOR_NOT_NULL") @Size(min = 1, message = "OPERATOR_NOT_EMPTY") String opearator,
			@NotNull(message = "CONDITION_NOT_NULL") String condition,
			@NotNull(message = "CONDITION_VALUE_NOT_NULL") String conditionValue, int slaExpiry, boolean isRecurring,
			int intervalTime) {
		super();
		this.opearator = opearator;
		this.condition = condition;
		this.conditionValue = conditionValue;
		this.slaExpiry = slaExpiry;
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

	public int getSlaExpiry() {
		return slaExpiry;
	}

	public void setSlaExpiry(int slaExpiry) {
		this.slaExpiry = slaExpiry;
	}

}
