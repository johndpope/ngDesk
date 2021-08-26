package com.ngdesk.modules.detail.layouts;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ngdesk.modules.rules.Condition;

public class FieldSettings {
	@JsonProperty("ACTION")
	private String action;

	@JsonProperty("CONDITIONS")
	private List<Condition> conditions;

	public FieldSettings(String action, List<Condition> conditions) {
		super();
		this.action = action;
		this.conditions = conditions;
	}

	public FieldSettings() {
		super();
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public List<Condition> getConditions() {
		return conditions;
	}

	public void setConditions(List<Condition> conditions) {
		this.conditions = conditions;
	}

}
