package com.ngdesk.modules.channels.chatbots;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ChatbotValue {

	@JsonProperty("MESSAGE")
	private String message;

	@JsonProperty("OPTIONS")
	private List<Option> options;

	@JsonProperty("MAPPING")
	private String mapping;

	@JsonProperty("DISABLE_TEXT_INPUT")
	private Boolean disableTextInput;

	public ChatbotValue() {

	}

	public ChatbotValue(String message, List<Option> options, String mapping, Boolean disableTextInput) {
		super();
		this.message = message;
		this.options = options;
		this.mapping = mapping;
		this.disableTextInput = disableTextInput;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public List<Option> getOptions() {
		return options;
	}

	public void setOptions(List<Option> options) {
		this.options = options;
	}

	public String getMapping() {
		return mapping;
	}

	public void setMapping(String mapping) {
		this.mapping = mapping;
	}

	public Boolean getDisableTextInput() {
		return disableTextInput;
	}

	public void setDisableTextInput(Boolean disableTextInput) {
		this.disableTextInput = disableTextInput;
	}

}
