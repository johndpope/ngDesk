package com.ngdesk.graphql.chat.channel.dao;

import java.util.Date;
import java.util.List;

import org.springframework.data.mongodb.core.mapping.Field;

public class ChatPrompt {

	@Field("NAME")
	private String promptName;

	@Field("DESCRIPTION")
	private String promptdescription;

	@Field("PROMPT_ID")
	private String promptId;

	@Field("CONDITIONS")
	private List<Condition> conditions;

	@Field("CHAT_PROMPT_ACTION")
	private ChatPromptAction chatPromptAction;

	@Field("DATE_UPDATED")
	private Date dateUpdated;

	@Field("LAST_UPDATED_BY")
	private String lastUpdatedBy;

<<<<<<< Updated upstream
=======
	@Field("RUN_TRIGGER")
	private String runTrigger;

	@Field("CHAT_PROMPT_ACTION")
	private ChatPromptAction action;

>>>>>>> Stashed changes
	public ChatPrompt() {

	}

	public ChatPrompt(String promptName, String promptdescription, String promptId, List<Condition> conditions,
<<<<<<< Updated upstream
			ChatPromptAction chatPromptAction, Date dateUpdated, String lastUpdatedBy) {
=======
			Date dateUpdated, String lastUpdatedBy, String runTrigger,
			com.ngdesk.graphql.chat.channel.dao.ChatPromptAction action) {
>>>>>>> Stashed changes
		super();
		this.promptName = promptName;
		this.promptdescription = promptdescription;
		this.promptId = promptId;
		this.conditions = conditions;
		this.chatPromptAction = chatPromptAction;
		this.dateUpdated = dateUpdated;
		this.lastUpdatedBy = lastUpdatedBy;
<<<<<<< Updated upstream
=======
		this.runTrigger = runTrigger;
		this.action = action;
>>>>>>> Stashed changes
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

	public List<Condition> getConditions() {
		return conditions;
	}

	public void setConditions(List<Condition> conditions) {
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

<<<<<<< Updated upstream
=======
	public String getRunTrigger() {
		return runTrigger;
	}

	public void setRunTrigger(String runTrigger) {
		this.runTrigger = runTrigger;
	}

	public ChatPromptAction getAction() {
		return action;
	}

	public void setAction(ChatPromptAction action) {
		this.action = action;
	}

>>>>>>> Stashed changes
}
