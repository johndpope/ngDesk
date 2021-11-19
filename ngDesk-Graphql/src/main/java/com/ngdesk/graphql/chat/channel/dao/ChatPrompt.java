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

	@Field("DATE_UPDATED")
	private Date dateUpdated;

	@Field("LAST_UPDATED_BY")
	private String lastUpdatedBy;

	@Field("RUN_TRIGGER")
	private String runTrigger;

	public ChatPrompt() {

	}

	public ChatPrompt(String promptName, String promptdescription, String promptId, List<Condition> conditions,
			Date dateUpdated, String lastUpdatedBy, String runTrigger) {
		super();
		this.promptName = promptName;
		this.promptdescription = promptdescription;
		this.promptId = promptId;
		this.conditions = conditions;
		this.dateUpdated = dateUpdated;
		this.lastUpdatedBy = lastUpdatedBy;
		this.runTrigger = runTrigger;
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

	public String getRunTrigger() {
		return runTrigger;
	}

	public void setRunTrigger(String runTrigger) {
		this.runTrigger = runTrigger;
	}

}
