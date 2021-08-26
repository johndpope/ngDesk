package com.ngdesk.channels.chat.triggers;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ngdesk.annotations.ChatTriggerConditionValid;
import com.ngdesk.annotations.TriggerCondition;

@TriggerCondition
public class Condition {

	@JsonProperty("REQUIREMENT_TYPE")
	@NotEmpty(message = "REQUIREMENT_TYPE_REQUIRED")
	@Pattern(regexp = "All|Any", message = "NOT_VALID_REQUIREMENT_TYPE")
	private String requirementType;

	@JsonProperty("CONDITION")
	@NotEmpty(message = "CONDITION_REQUIRED")
	@ChatTriggerConditionValid
	private String condition;

	@JsonProperty("OPERATOR")
	@NotEmpty(message = "OPERATOR_REQUIRED")
	private String operator;

	@JsonProperty("CONDITION_VALUE")
	@NotEmpty(message = "CONDITION_VALUE_REQUIRED")
	private String value;

	@JsonProperty("DATA_TYPE")
	@NotEmpty(message = "DATA_TYPE_REQUIRED")
	@Pattern(regexp = "String|Integer", message = "NOT_VALID_DATA_TYPE")
	private String dataType;

	public Condition() {

	}

	public Condition(
			@NotEmpty(message = "REQUIREMENT_TYPE_REQUIRED") @Pattern(regexp = "All|Any", message = "NOT_VALID_REQUIREMENT_TYPE") String requirementType,
			@NotEmpty(message = "CONDITION_REQUIRED") String condition,
			@NotEmpty(message = "OPERATOR_REQUIRED") String operator,
			@NotEmpty(message = "CONDITION_VALUE_REQUIRED") String value,
			@NotEmpty(message = "DATA_TYPE_REQUIRED") @Pattern(regexp = "String|Integer", message = "NOT_VALID_DATA_TYPE") String dataType) {
		super();
		this.requirementType = requirementType;
		this.condition = condition;
		this.operator = operator;
		this.value = value;
		this.dataType = dataType;
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

	public String getDataType() {
		return dataType;
	}

	public void setDataType(String dataType) {
		this.dataType = dataType;
	}

	public String getRequirementType() {
		return requirementType;
	}

	public void setRequirementType(String requirementType) {
		this.requirementType = requirementType;
	}

}
