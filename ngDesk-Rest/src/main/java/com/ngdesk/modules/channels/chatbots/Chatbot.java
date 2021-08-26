package com.ngdesk.modules.channels.chatbots;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ngdesk.modules.channels.chatbots.ChatbotWorkflow;

public class Chatbot {

	@JsonProperty("NAME")
	@NotNull(message = "CHATBOT_NAME_NOT_NULL")
	@Size(min = 1, message = "CHATBOT_NAME_NOT_EMPTY")
	@Pattern(regexp = "(([A-Za-z0-9\\s])+)", message = "INVALID_CHATBOT_NAME")
	private String name;

	@JsonProperty("DESCRIPTION")
	@NotNull(message = "CHATBOT_DESCRIPTION_NOT_NULL")
	private String description;

	@JsonProperty("WORKFLOW")
	@NotNull(message = "CHATBOT_WORKFLOW_NOT_NULL")
	private ChatbotWorkflow workflow;

	@JsonProperty("CHAT_BOT_ID")
	private String chatbotId;

	public Chatbot() {
	}

	public Chatbot(
			@NotNull(message = "CHATBOT_NAME_NOT_NULL") @Size(min = 1, message = "CHATBOT_NAME_NOT_EMPTY") @Pattern(regexp = "(([A-Za-z0-9\\s])+)", message = "INVALID_SLA_NAME") String name,
			@NotNull(message = "CHATBOT_DESCRIPTION_NOT_NULL") String description,
			@NotNull(message = "CHATBOT_WORKFLOW_NOT_NULL") ChatbotWorkflow workflow, String chatbotId) {
		super();
		this.name = name;
		this.description = description;
		this.workflow = workflow;
		this.chatbotId = chatbotId;
	}


	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public ChatbotWorkflow getWorkflow() {
		return workflow;
	}

	public void setWorkflow(ChatbotWorkflow workflow) {
		this.workflow = workflow;
	}

	public String getChatbotId() {
		return chatbotId;
	}

	public void setChatbotId(String chatbotId) {
		this.chatbotId = chatbotId;
	}

}
