package com.ngdesk.modules.detail.layouts;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ngdesk.modules.rules.Condition;

public class Settings {

	@JsonProperty("COLLAPSABLE")
	private boolean collapsable;

	@JsonProperty("ACTION")
	private String action;

	@JsonProperty("CONDITIONS")
	private List<Condition> conditions;

	public Settings() {
	}

	public Settings(boolean collapsable, String action, List<Condition> conditions) {
		super();
		this.collapsable = collapsable;
		this.action = action;
		this.conditions = conditions;
	}

	public boolean isCollapsable() {
		return collapsable;
	}

	public void setCollapsable(boolean collapsable) {
		this.collapsable = collapsable;
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
