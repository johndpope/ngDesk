package com.ngdesk.module.channels.chat;

import java.util.Date;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ChatPrompt {

	@NotNull(message = "NAME_NOT_NULL")
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
	@Valid
	private List<Conditions> conditions;

	@JsonProperty("CHAT_PROMPT_ACTION")
	@Field("CHAT_PROMPT_ACTION")
<<<<<<< Updated upstream
	private ChatPromptAction chatPromptAction;
=======
	private Action action;
>>>>>>> Stashed changes

	@JsonProperty("DATE_UPDATED")
	@Field("DATE_UPDATED")
	private Date dateUpdated;

	@JsonProperty("LAST_UPDATED_BY")
	@Field("LAST_UPDATED_BY")
	private String lastUpdatedBy;

	public ChatPrompt() {

	}

	public ChatPrompt(@NotNull(message = "NAME_NOT_NULL") String promptName, String promptdescription, String promptId,
<<<<<<< Updated upstream
			@Valid List<Conditions> conditions, ChatPromptAction chatPromptAction, Date dateUpdated,
			String lastUpdatedBy) {
=======
			@Valid List<Conditions> conditions, Action action, Date dateUpdated, String lastUpdatedBy,
			@NotNull(message = "TRIGGERS_NOT_NULL") @Pattern(regexp = "LOADED_CHAT_WIDGET|REQUESTS_CHAT|MESSAGE_SENT", message = "INVALID_RUN_TRIGGER") String runTrigger) {
>>>>>>> Stashed changes
		super();
		this.promptName = promptName;
		this.promptdescription = promptdescription;
		this.promptId = promptId;
		this.conditions = conditions;
<<<<<<< Updated upstream
		this.chatPromptAction = chatPromptAction;
=======
		this.action = action;
>>>>>>> Stashed changes
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

<<<<<<< Updated upstream
	public ChatPromptAction getChatPromptAction() {
		return chatPromptAction;
	}

	public void setChatPromptAction(ChatPromptAction chatPromptAction) {
		this.chatPromptAction = chatPromptAction;
=======
	public Action getAction() {
		return action;
	}

	public void setAction(Action action) {
		this.action = action;
>>>>>>> Stashed changes
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

<<<<<<< Updated upstream
}
=======
	public String getRunTrigger() {
		return runTrigger;
	}

	public void setRunTrigger(String runTrigger) {
		this.runTrigger = runTrigger;
	}

}
>>>>>>> Stashed changes
