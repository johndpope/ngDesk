package com.ngdesk.modules.slas;

import java.util.Date;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ngdesk.workflow.Workflow;

public class Sla {

	@JsonProperty("NAME")
	@NotNull(message = "SLA_NAME_NOT_NULL")
	@Size(min = 1, message = "SLA_NAME_NOT_EMPTY")
	@Pattern(regexp = "(([A-Za-z0-9\\s])+)", message = "INVALID_SLA_NAME")
	private String name;

	@JsonProperty("DESCRIPTION")
	@NotNull(message = "SLA_DESCRIPTION_NOT_NULL")
	private String description;

	@JsonProperty("CONDITIONS")
	@NotNull(message = "CONDITIONS_NOT_NULL")
	@Valid
	private List<SlaConditions> conditions;

	@JsonProperty("SLA_ID")
	private String slaId;

	@JsonProperty("DATE_CREATED")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
	private Date dateCreated;

	@JsonProperty("DATE_UPDATED")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
	private Date dateUpdated;

	@JsonProperty("LAST_UPDATED_BY")
	private String lastUpdatedBy;

	@JsonProperty("CREATED_BY")
	private String createdBy;

	@JsonProperty("VIOLATIONS")
	@Valid
	@Size(min = 1, max = 1, message = "SLA_VIOLATION_NOT_EMPTY")
	private List<Violation> violations;

	@JsonProperty("WORKFLOW")
	@Valid
	private Workflow workflow;

	@JsonProperty("DELETED")
	private boolean deleted;

	@JsonProperty("IS_RECURRING")
	private boolean recurring;

	@JsonProperty("MAX_RECCURENCE")
	private int maxRecurrence;

	@JsonProperty("INTERVAL_TIME")
	@Min(value = 0, message = "INTERVAL_TIME_ERROR_MESSAGE")
	private int intervalTime;

	@JsonProperty("BUSINESS_RULES")
	@Valid
	private SlaBuisnessRules slaBuisinessRules;

	public Sla() {

	}

	public Sla(
			@NotNull(message = "SLA_NAME_NOT_NULL") @Size(min = 1, message = "SLA_NAME_NOT_EMPTY") @Pattern(regexp = "(([A-Za-z0-9\\s])+)", message = "INVALID_SLA_NAME") String name,
			@NotNull(message = "SLA_DESCRIPTION_NOT_NULL") String description,
			@NotNull(message = "CONDITIONS_NOT_NULL") @Valid List<SlaConditions> conditions, String slaId,
			Date dateCreated, Date dateUpdated, String lastUpdatedBy, String createdBy,
			@Valid @Size(min = 1, max = 1, message = "SLA_VIOLATION_NOT_EMPTY") List<Violation> violations,
			@Valid Workflow workflow, boolean deleted, boolean recurring, int maxRecurrence,
			@Min(value = 0, message = "INTERVAL_TIME_ERROR_MESSAGE") int intervalTime,
			@Valid SlaBuisnessRules slaBuisinessRules) {
		super();
		this.name = name;
		this.description = description;
		this.conditions = conditions;
		this.slaId = slaId;
		this.dateCreated = dateCreated;
		this.dateUpdated = dateUpdated;
		this.lastUpdatedBy = lastUpdatedBy;
		this.createdBy = createdBy;
		this.violations = violations;
		this.workflow = workflow;
		this.deleted = deleted;
		this.recurring = recurring;
		this.maxRecurrence = maxRecurrence;
		this.intervalTime = intervalTime;
		this.slaBuisinessRules = slaBuisinessRules;
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

	public List<SlaConditions> getConditions() {
		return conditions;
	}

	public void setConditions(List<SlaConditions> conditions) {
		this.conditions = conditions;
	}

	public String getSlaId() {
		return slaId;
	}

	public void setSlaId(String slaId) {
		this.slaId = slaId;
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

	public String getLastUpdatedBy() {
		return lastUpdatedBy;
	}

	public void setLastUpdatedBy(String lastUpdatedBy) {
		this.lastUpdatedBy = lastUpdatedBy;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public List<Violation> getViolations() {
		return violations;
	}

	public void setViolations(List<Violation> violations) {
		this.violations = violations;
	}

	public Workflow getWorkflow() {
		return workflow;
	}

	public void setWorkflow(Workflow workflow) {
		this.workflow = workflow;
	}

	public boolean isDeleted() {
		return deleted;
	}

	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}

	public boolean isRecurring() {
		return recurring;
	}

	public void setRecurring(boolean recurring) {
		this.recurring = recurring;
	}

	public int getMaxRecurrence() {
		return maxRecurrence;
	}

	public void setMaxRecurrence(int maxRecurrence) {
		this.maxRecurrence = maxRecurrence;
	}

	public int getIntervalTime() {
		return intervalTime;
	}

	public void setIntervalTime(int intervalTime) {
		this.intervalTime = intervalTime;
	}

	public SlaBuisnessRules getSlaBuisinessRules() {
		return slaBuisinessRules;
	}

	public void setSlaBuisinessRules(SlaBuisnessRules slaBuisinessRules) {
		this.slaBuisinessRules = slaBuisinessRules;
	}

}
