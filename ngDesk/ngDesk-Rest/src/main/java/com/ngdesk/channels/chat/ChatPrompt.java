package com.ngdesk.channels.chat;

import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import com.ngdesk.annotations.ValidChatPromptTriggers;
import com.ngdesk.workflow.Workflow;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ChatPrompt {

	@NotNull(message = "NAME_NOT_NULL")
	@JsonProperty("NAME")
	private String promptName;

	@JsonProperty("DESCRIPTION")
	private String promptdescription;

	@JsonProperty("PROMPT_ID")
	private String promptId;

	@JsonProperty("CONDITIONS")
	@Valid
	private List<Conditions> conditions;

	@JsonProperty("WORKFLOW")
	private Workflow workflow;

	@JsonProperty("DATE_UPDATED")
	private String dateUpdated;

	@JsonProperty("LAST_UPDATED_BY")
	private String lastUpdatedBy;

	@NotNull(message = "TRIGGERS_NOT_NULL")
	@JsonProperty("RUN_TRIGGER")
	@ValidChatPromptTriggers
	private String runTrigger;

	public ChatPrompt() {

	}

	public ChatPrompt(@NotNull(message = "NAME_NOT_NULL") String promptName, String promptdescription, String promptId,
			@Valid List<Conditions> conditions, Workflow workflow, String dateUpdated, String lastUpdatedBy,
			@NotNull(message = "TRIGGERS_NOT_NULL") String runTrigger) {
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

	public String getDateUpdated() {
		return dateUpdated;
	}

	public void setDateUpdated(String dateUpdated) {
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
