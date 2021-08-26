package com.ngdesk.module.layout.dao;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;


public class Settings {

	@Field("COLLAPSABLE")
	@JsonProperty("COLLAPSABLE")
	private boolean collapsable=false;

	@Field("ACTION")
	@JsonProperty("ACTION")
	private String action="";

	@Field("CONDITIONS")
	@JsonProperty("CONDITIONS")
	private List<Condition> conditions=new ArrayList<>();

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

