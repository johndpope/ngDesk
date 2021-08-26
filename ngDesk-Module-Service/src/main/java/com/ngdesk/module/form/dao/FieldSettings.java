package com.ngdesk.module.form.dao;

import java.util.ArrayList;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.AccessMode;

public class FieldSettings {

	@Schema(description = "Action", required = false)
	private String action = "";

	@Schema(description = "Conditions", required = true)
	private List<Condition> conditions = new ArrayList<>();

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
