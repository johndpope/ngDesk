package com.ngdesk.graphql.form.dao;

import java.util.ArrayList;
import java.util.List;

public class FieldSettings {

	private String action;

	private List<FormCondition> conditions = new ArrayList<>();

	public FieldSettings() {

	}

	public FieldSettings(String action, List<FormCondition> conditions) {
		super();
		this.action = action;
		this.conditions = conditions;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public List<FormCondition> getConditions() {
		return conditions;
	}

	public void setConditions(List<FormCondition> conditions) {
		this.conditions = conditions;
	}

}
