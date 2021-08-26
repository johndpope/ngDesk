package com.ngdesk.modules.monitors;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ngdesk.annotations.ValidAction;
import com.ngdesk.annotations.ValidValue;

@ValidValue
public class MonitorAction {

	@JsonProperty("ACTION")
	@NotNull(message = "ACTION_NAME_NOT_NULL")
	@ValidAction
	@Size(min = 1, message = "ACTION_NAME_NOT_EMPTY")
	private String action;

	@JsonProperty("VALUES")
	@NotNull(message = "VALUES_NOT_NULL")
	@Valid
	private List<Value> values;

	public MonitorAction() {

	}

	public MonitorAction(
			@NotNull(message = "ACTION_NAME_NOT_NULL") @Size(min = 1, message = "ACTION_NAME_NOT_EMPTY") String action,
			@NotNull(message = "VALUES_NOT_NULL") @Valid List<Value> values) {
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
