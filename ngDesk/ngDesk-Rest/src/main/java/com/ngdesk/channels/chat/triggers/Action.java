package com.ngdesk.channels.chat.triggers;

import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ngdesk.annotations.ChatTriggerActionValid;

public class Action {

	@JsonProperty("ACTION")
	@ChatTriggerActionValid
	private String action;

	@JsonProperty("VALUES")
	@NotEmpty(message = "VALUES_REQUIRED")
	@Valid
	private List<Value> values = new ArrayList<Value>();

	public Action() {

	}

	public Action(String action, @NotEmpty(message = "VALUES_REQUIRED") @Valid List<Value> values) {
		super();
		this.action = action;
		this.values = values;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public List<Value> getValues() {
		return values;
	}

	public void setValues(List<Value> values) {
		this.values = values;
	}

}
