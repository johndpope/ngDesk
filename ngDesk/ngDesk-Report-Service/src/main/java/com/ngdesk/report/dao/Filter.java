package com.ngdesk.report.dao;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import com.ngdesk.commons.annotations.CustomNotEmpty;

public class Filter {

	@NotNull(message = "FILTER_FIELD")
	@Valid
	private ReportField field;

	@CustomNotEmpty(message = "DAO_VARIABLE_REQUIRED", values = { "OPERATOR" })
	@Pattern(regexp = "CHANGED|EQUALS_TO|NOT_EQUALS_TO|CONTAINS|DOES_NOT_CONTAIN|REGEX|LESS_THAN|GREATER_THAN|LENGTH_GREATER_THAN|LENGTH_LESS_THAN", message = "NOT_VALID_OPERATOR")
	private String operator;

	@Pattern(regexp = "All|Any", message = "INVALID_REQUIREMENT_TYPE")
	private String requirementType;

	
	@CustomNotEmpty(message = "DAO_VARIABLE_REQUIRED", values = { "VALUE" })
	private String value;

	public Filter() {

	}

	public Filter(@NotNull(message = "FILTER_FIELD") @Valid ReportField field,
			@Pattern(regexp = "CHANGED|EQUALS_TO|NOT_EQUALS_TO|CONTAINS|DOES_NOT_CONTAIN|REGEX|LESS_THAN|GREATER_THAN|LENGTH_GREATER_THAN|LENGTH_LESS_THAN", message = "NOT_VALID_OPERATOR") String operator,
			@Pattern(regexp = "All|Any", message = "INVALID_REQUIREMENT_TYPE") String requirementType, String value) {
		super();
		this.field = field;
		this.operator = operator;
		this.requirementType = requirementType;
		this.value = value;
	}

	public ReportField getField() {
		return field;
	}

	public void setField(ReportField field) {
		this.field = field;
	}

	public String getOperator() {
		return operator;
	}

	public void setOperator(String operator) {
		this.operator = operator;
	}

	public String getRequirementType() {
		return requirementType;
	}

	public void setRequirementType(String requirementType) {
		this.requirementType = requirementType;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

}
