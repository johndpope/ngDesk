package com.ngdesk.websocket.workflow.dao;

import java.util.Date;
import java.util.List;

import org.springframework.data.annotation.Id;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Workflow {

	@Id
	@JsonProperty("WORKFLOW_ID")
	private String id;

	@JsonProperty("MODULE_ID")
	private String moduleId;

	@JsonProperty("COMPANY_ID")
	private String companyId;

	@JsonProperty("TYPE")
	private String type;

	@JsonProperty("NAME")
	private String name;

	@JsonProperty("DESCRIPTION")
	private String description;

	@JsonProperty("CONDITIONS")
	List<Condition> conditions;

	@JsonProperty("STAGES")
	private List<Stage> stages;

	@JsonProperty("ORDER")
	private Integer order;

	@JsonProperty("DATE_CREATED")
	private Date dateCreated;

	@JsonProperty("DATE_UPDATED")
	private Date dateUpdated;

	@JsonProperty("LAST_UPDATED_BY")
	private String lastUpdated;

	@JsonProperty("CREATED_BY")
	private String createdBy;

	@JsonProperty("RAPID_UI_PAYLOAD")
	private String rapidUiPayload;

	@JsonProperty("DISPLAY_ON_ENTRY")
	private boolean displayOnEntry;

	public Workflow() {

	}

	public Workflow(String id, String moduleId, String companyId, String type, String name, String description,
			List<Condition> conditions, List<Stage> stages, Integer order, Date dateCreated, Date dateUpdated,
			String lastUpdated, String createdBy, String rapidUiPayload, boolean displayOnEntry) {
		super();
		this.id = id;
		this.moduleId = moduleId;
		this.companyId = companyId;
		this.type = type;
		this.name = name;
		this.description = description;
		this.conditions = conditions;
		this.stages = stages;
		this.order = order;
		this.dateCreated = dateCreated;
		this.dateUpdated = dateUpdated;
		this.lastUpdated = lastUpdated;
		this.createdBy = createdBy;
		this.rapidUiPayload = rapidUiPayload;
		this.displayOnEntry = displayOnEntry;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getModuleId() {
		return moduleId;
	}

	public void setModuleId(String moduleId) {
		this.moduleId = moduleId;
	}

	public String getCompanyId() {
		return companyId;
	}

	public void setCompanyId(String companyId) {
		this.companyId = companyId;
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

	public List<Stage> getStages() {
		return stages;
	}

	public void setStages(List<Stage> stages) {
		this.stages = stages;
	}

	public Integer getOrder() {
		return order;
	}

	public void setOrder(Integer order) {
		this.order = order;
	}

	public Date getDateCreated() {
		return dateCreated;
	}

	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}

	public Date getDateUpdated() {
		return dateUpdated;
	}

	public void setDateUpdated(Date dateUpdated) {
		this.dateUpdated = dateUpdated;
	}

	public String getLastUpdated() {
		return lastUpdated;
	}

	public void setLastUpdated(String lastUpdated) {
		this.lastUpdated = lastUpdated;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public String getRapidUiPayload() {
		return rapidUiPayload;
	}

	public void setRapidUiPayload(String rapidUiPayload) {
		this.rapidUiPayload = rapidUiPayload;
	}

	public boolean isDisplayOnEntry() {
		return displayOnEntry;
	}

	public void setDisplayOnEntry(boolean displayOnEntry) {
		this.displayOnEntry = displayOnEntry;
	}
}