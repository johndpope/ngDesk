package com.ngdesk.graphql.chat.channel.dao;

import org.springframework.data.mongodb.core.mapping.Field;

public class ChatPromptAction {

	@Field("ACTION_NAME")
	private String actionName;

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
