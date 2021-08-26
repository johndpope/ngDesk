package com.ngdesk.modules.workflows;

import java.sql.Timestamp;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ngdesk.modules.rules.Condition;
import com.ngdesk.workflow.Workflow;

public class ModuleWorkflow {

	@JsonProperty("TYPE")
	@NotNull(message = "WORKFLOW_TYPE_NOT_NULL")
	@Size(min = 1, message = "WORKFLOW_TYPE_NOT_EMPTY")
	@Pattern(regexp = "CREATE|UPDATE|DELETE|CREATE_OR_UPDATE", message = "INVALID_WORKFLOW_TYPE")
	private String type;

	@JsonProperty("NAME")
	@NotNull(message = "WORKFLOW_NAME_NOT_NULL")
	@Size(min = 1, message = "WORKFLOW_NAME_NOT_EMPTY")
	private String name;

	@JsonProperty("DESCRIPTION")
	@NotNull(message = "WORKFLOW_DESCRIPTION_NOT_NULL")
	private String description;

	@JsonProperty("CONDITIONS")
	@NotNull(message = "CONDITIONS_NOT_NULL")
	@Valid
	private List<Condition> conditions;

	@JsonProperty("WORKFLOW_ID")
	private String workflowId;

	@JsonProperty("WORKFLOW")
	@Valid
	private Workflow workflow;

	@JsonProperty("DATE_CREATED")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
	private Timestamp dateCreated;

	@JsonProperty("DATE_UPDATED")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
	private Timestamp dateUpdated;

	@JsonProperty("LAST_UPDATED_BY")
	private String lastUpdatedBy;
	
	@JsonProperty("ORDER")
	@NotNull(message = "WORKFLOW_ORDER_NOT_NULL")
	@Min(value = 0, message = "WORKFLOW_ORDER_IS_NULL")
    private int order;

	public ModuleWorkflow() {

	}

	public ModuleWorkflow(
			@NotNull(message = "WORKFLOW_TYPE_NOT_NULL") @Size(min = 1, message = "WORKFLOW_TYPE_NOT_EMPTY") @Pattern(regexp = "CREATE|UPDATE|DELETE|CREATE_OR_UPDATE", message = "INVALID_WORKFLOW_TYPE") String type,
			@NotNull(message = "WORKFLOW_NAME_NOT_NULL") @Size(min = 1, message = "WORKFLOW_NAME_NOT_EMPTY") String name,
			@NotNull(message = "WORKFLOW_DESCRIPTION_NOT_NULL") String description,
			@NotNull(message = "CONDITIONS_NOT_NULL") @Valid List<Condition> conditions, String workflowId,
			@Valid Workflow workflow, Timestamp dateCreated, Timestamp dateUpdated, String lastUpdatedBy,
			@NotNull(message = "WORKFLOW_ORDER_NOT_NULL") @Min(value = 0, message = "WORKFLOW_ORDER_IS_NULL") int order) {
		super();
		this.type = type;
		this.name = name;
		this.description = description;
		this.conditions = conditions;
		this.workflowId = workflowId;
		this.workflow = workflow;
		this.dateCreated = dateCreated;
		this.dateUpdated = dateUpdated;
		this.lastUpdatedBy = lastUpdatedBy;
		this.order = order;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
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

	public List<Condition> getConditions() {
		return conditions;
	}

	public void setConditions(List<Condition> conditions) {
		this.conditions = conditions;
	}

	public String getWorkflowId() {
		return workflowId;
	}

	public void setWorkflowId(String workflowId) {
		this.workflowId = workflowId;
	}

	public Workflow getWorkflow() {
		return workflow;
	}

	public void setWorkflow(Workflow workflow) {
		this.workflow = workflow;
	}

	public Timestamp getDateCreated() {
		return dateCreated;
	}

	public void setDateCreated(Timestamp dateCreated) {
		this.dateCreated = dateCreated;
	}

	public Timestamp getDateUpdated() {
		return dateUpdated;
	}

	public void setDateUpdated(Timestamp dateUpdated) {
		this.dateUpdated = dateUpdated;
	}

	public String getLastUpdatedBy() {
		return lastUpdatedBy;
	}

	public void setLastUpdatedBy(String lastUpdatedBy) {
		this.lastUpdatedBy = lastUpdatedBy;
	}

	public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		this.order = order;
	}

	
		

}
