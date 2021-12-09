package com.ngdesk.module.channels.chat;

import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Action {

	@Field("ACTION_NAME")
	private String actionName;

	@Field("ACTION_VALUE")
	private String actionValue;

	public Action(String actionName, String actionValue) {
		super();
		this.actionName = actionName;
		this.actionValue = actionValue;
	}

	public String getActionName() {
		return actionName;
	}

	public void setActionName(String actionName) {
		this.actionName = actionName;
	}

	public String getActionValue() {
		return actionValue;
	}

	public void setActionValue(String actionValue) {
		this.actionValue = actionValue;
	}

}