package com.ngdesk.modules.rules;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Action {

	@JsonProperty("ACTION")
	@NotEmpty(message = "ACTION_NAME_NOT_EMPTY")
	@Pattern(regexp = "Show|Hide", message = "INVALID_ACTION")
	private String action;

	@JsonProperty("FIELD")
	@NotEmpty(message = "FIELD_NOT_EMPTY")
	@Valid
	private String field;

	public Action() {

	}

	public Action(
			@NotEmpty(message = "ACTION_NAME_NOT_EMPTY") @Pattern(regexp = "Show|Hide", message = "INVALID_ACTION") String action,
			@NotEmpty(message = "FIELD_NOT_EMPTY") @Valid String field) {
		super();
		this.action = action;
		this.field = field;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public String getField() {
		return field;
	}

	public void setField(String field) {
		this.field = field;
	}

}
