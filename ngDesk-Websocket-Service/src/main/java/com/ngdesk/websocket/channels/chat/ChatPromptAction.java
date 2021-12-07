package com.ngdesk.websocket.channels.chat;

import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ChatPromptAction {

	@JsonProperty("ACTION_NAME")
	@Field("ACTION_NAME")
	private String actionName;

	@JsonProperty("ACTION_VALUE")
	@Field("ACTION_VALUE")
	private String actionValue;

	public ChatPromptAction() {
		super();
	}

	public ChatPromptAction(String actionName, String actionValue) {
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
