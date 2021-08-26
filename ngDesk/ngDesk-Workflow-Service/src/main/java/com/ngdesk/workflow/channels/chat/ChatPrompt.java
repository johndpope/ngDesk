package com.ngdesk.workflow.channels.chat;

import java.util.Date;
import java.util.List;

import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ngdesk.workflow.dao.Workflow;

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

	@JsonProperty("WORKFLOW")
	@Field("WORKFLOW")
	private Workflow workflow;

	@JsonProperty("DATE_UPDATED")
	@Field("DATE_UPDATED")
	private Date dateUpdated;

	@JsonProperty("LAST_UPDATED_BY")
	@Field("LAST_UPDATED_BY")
	private String lastUpdatedBy;

	@JsonProperty("RUN_TRIGGER")
	@Field("RUN_TRIGGER")
	private String runTrigger;

	public ChatPrompt() {

	}

	public ChatPrompt(String promptName, String promptdescription, String promptId, List<Conditions> conditions,
			Workflow workflow, Date dateUpdated, String lastUpdatedBy, String runTrigger) {
		super();
		this.promptName = promptName;
		this.promptdescription = promptdescription;
		this.promptId = promptId;
		this.conditions = conditions;
		this.workflow = workflow;
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

	public List<Conditions> getConditions() {
		return conditions;
	}

	public void setConditions(List<Conditions> conditions) {
		this.conditions = conditions;
	}

	public Workflow getWorkflow() {
		return workflow;
	}

	public void setWorkflow(Workflow workflow) {
		this.workflow = workflow;
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
