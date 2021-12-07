package com.ngdesk.module.channels.chat;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ChatPromptAction {

	@NotNull(message = "ACTION_NAME_NOT_NULL")
	@JsonProperty("ACTION_NAME")
	@Field("ACTION_NAME")
	@Pattern(regexp = "LOADED_CHAT_WIDGET|REQUESTS_CHAT|MESSAGE_SENT", message = "INVALID_ACTION_NAME")
	private String actionName;

	@JsonProperty("ACTION_VALUE")
	@Field("ACTION_VALUE")
	private String actionValue;

	public ChatPromptAction() {
		super();
	}

	public ChatPromptAction(
			@NotNull(message = "ACTION_NAME_NOT_NULL") @Pattern(regexp = "LOADED_CHAT_WIDGET|REQUESTS_CHAT|MESSAGE_SENT", message = "INVALID_ACTION_NAME") String actionName,
			String actionValue) {
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
