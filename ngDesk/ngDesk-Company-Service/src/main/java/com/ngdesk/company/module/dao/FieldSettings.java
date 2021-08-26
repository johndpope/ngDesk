package com.ngdesk.company.module.dao;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;

public class FieldSettings {
	
	@Field("ACTION")
	@JsonProperty("ACTION")
	private String action="";

	@Field("CONDITIONS")
	@JsonProperty("CONDITIONS")
	private List<Condition> conditions= new ArrayList<>();

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
