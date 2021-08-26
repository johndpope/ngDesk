package com.ngdesk.reporting;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ngdesk.annotations.ReportingOperators;

public class Filter {

	@JsonProperty("FIELD")
	@NotNull(message = "FIELD_NOT_NULL")
	@Valid
	private Field field;

	@JsonProperty("OPERATOR")
	@ReportingOperators
	@NotNull(message = "OPERATOR_NOT_NULL")
	@Size(min = 1, message = "OPERATOR_EMPTY")
	private String operator;

	@JsonProperty("VALUE")
	@NotNull(message = "VALUE_NOT_NULL")
	@Size(min = 1, message = "VALUE_EMPTY")
	private String value;

	public Filter() {

	}

	public Filter(@NotNull(message = "FIELD_NOT_NULL") @Valid Field field,
			@NotNull(message = "OPERATOR_NOT_NULL") @Size(min = 1, message = "OPERATOR_EMPTY") String operator,
			@NotNull(message = "VALUE_NOT_NULL") @Size(min = 1, message = "VALUE_EMPTY") String value) {
		super();
		this.field = field;
		this.operator = operator;
		this.value = value;
	}

	public Field getField() {
		return field;
	}

	public void setField(Field field) {
		this.field = field;
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
