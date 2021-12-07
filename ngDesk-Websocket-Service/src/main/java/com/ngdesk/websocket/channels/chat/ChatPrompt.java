package com.ngdesk.websocket.channels.chat;

import java.util.Date;
import java.util.List;

import javax.validation.Valid;

import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ChatPrompt {

	@JsonProperty("NAME")
	@Field("NAME")
	private String promptName;

	@JsonProperty("DESCRIPTION")
	@Field("DESCRIPTION")
	private String promptdescription;

	@JsonProperty("PROMPT_ID")
	@Field("PROMPT_ID")
	private String promptId;

	@JsonProperty("CONDITIONS")
	@Field("CONDITIONS")
	private List<Conditions> conditions;

	@JsonProperty("CHAT_PROMPT_ACTION")
	@Field("CHAT_PROMPT_ACTION")
	private ChatPromptAction chatPromptAction;

	@JsonProperty("DATE_UPDATED")
	@Field("DATE_UPDATED")
	private Date dateUpdated;

	@JsonProperty("LAST_UPDATED_BY")
	@Field("LAST_UPDATED_BY")
	private String lastUpdatedBy;

	public ChatPrompt() {

	}

	public ChatPrompt(String promptName, String promptdescription, String promptId, @Valid List<Conditions> conditions,
			ChatPromptAction chatPromptAction, Date dateUpdated, String lastUpdatedBy) {
		super();
		this.promptName = promptName;
		this.promptdescription = promptdescription;
		this.promptId = promptId;
		this.conditions = conditions;
		this.chatPromptAction = chatPromptAction;
		this.dateUpdated = dateUpdated;
		this.lastUpdatedBy = lastUpdatedBy;
	}

	public String getPromptName() {
		return promptName;
	}

	public void setPromptName(String promptName) {
		this.promptName = promptName;
	}

	public String getPromptdescription() {
		return promptdescription;
	}

	public void setPromptdescription(String promptdescription) {
		this.promptdescription = promptdescription;
	}

	public String getPromptId() {
		return promptId;
	}

	public void setPromptId(String promptId) {
		this.promptId = promptId;
	}

	public List<Conditions> getConditions() {
		return conditions;
	}

	public void setConditions(List<Conditions> conditions) {
		this.conditions = conditions;
	}

	public ChatPromptAction getChatPromptAction() {
		return chatPromptAction;
	}

	public void setChatPromptAction(ChatPromptAction chatPromptAction) {
		this.chatPromptAction = chatPromptAction;
	}

	public Date getDateUpdated() {
		return dateUpdated;
	}

	public void setDateUpdated(Date dateUpdated) {
		this.dateUpdated = dateUpdated;
	}

	public String getLastUpdatedBy() {
		return lastUpdatedBy;
	}

	public void setLastUpdatedBy(String lastUpdatedBy) {
		this.lastUpdatedBy = lastUpdatedBy;
	}

}
