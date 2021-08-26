package com.ngdesk.graphql.slas.dao;

import java.util.Date;
import java.util.List;

import org.springframework.data.annotation.Id;

public class SLA {

	@Id
	private String slaId;

	private String companyId;

	private String moduleId;

	private String name;

	private String description;

	private List<SLAConditions> conditions;

	private Violation violation;

	private int slaExpiry;

	private boolean isRecurring;

	private Recurrence recurrence;

	private boolean isRestricted;

	private SLABusinessRules businessRules;

	private String workflow;

	private Date dateCreated;

	private Date dateUpdated;

	private String lastUpdatedBy;

	private String createdBy;

	private boolean deleted;

	public SLA() {

	}

	public SLA(String slaId, String companyId, String moduleId, String name, String description,
			List<SLAConditions> conditions, Violation violation, int slaExpiry, boolean isRecurring,
			Recurrence recurrence, boolean isRestricted, SLABusinessRules businessRules, String workflow,
			Date dateCreated, Date dateUpdated, String lastUpdatedBy, String createdBy, boolean deleted) {
		super();
		this.slaId = slaId;
		this.companyId = companyId;
		this.moduleId = moduleId;
		this.name = name;
		this.description = description;
		this.conditions = conditions;
		this.violation = violation;
		this.slaExpiry = slaExpiry;
		this.isRecurring = isRecurring;
		this.recurrence = recurrence;
		this.isRestricted = isRestricted;
		this.businessRules = businessRules;
		this.workflow = workflow;
		this.dateCreated = dateCreated;
		this.dateUpdated = dateUpdated;
		this.lastUpdatedBy = lastUpdatedBy;
		this.createdBy = createdBy;
		this.deleted = deleted;
	}

	public String getSlaId() {
		return slaId;
	}

	public void setSlaId(String slaId) {
		this.slaId = slaId;
	}

	public String getCompanyId() {
		return companyId;
	}

	public void setCompanyId(String companyId) {
		this.companyId = companyId;
	}

	public String getModuleId() {
		return moduleId;
	}

	public void setModuleId(String moduleId) {
		this.moduleId = moduleId;
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

	public List<SLAConditions> getConditions() {
		return conditions;
	}

	public void setConditions(List<SLAConditions> conditions) {
		this.conditions = conditions;
	}

	public Violation getViolation() {
		return violation;
	}

	public void setViolation(Violation violation) {
		this.violation = violation;
	}

	public int getSlaExpiry() {
		return slaExpiry;
	}

	public void setSlaExpiry(int slaExpiry) {
		this.slaExpiry = slaExpiry;
	}

	public boolean isRecurring() {
		return isRecurring;
	}

	public void setRecurring(boolean isRecurring) {
		this.isRecurring = isRecurring;
	}

	public Recurrence getRecurrence() {
		return recurrence;
	}

	public void setRecurrence(Recurrence recurrence) {
		this.recurrence = recurrence;
	}

	public boolean isRestricted() {
		return isRestricted;
	}

	public void setRestricted(boolean isRestricted) {
		this.isRestricted = isRestricted;
	}

	public SLABusinessRules getBusinessRules() {
		return businessRules;
	}

	public void setBusinessRules(SLABusinessRules businessRules) {
		this.businessRules = businessRules;
	}

	public String getWorkflow() {
		return workflow;
	}

	public void setWorkflow(String workflow) {
		this.workflow = workflow;
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

	public boolean isDeleted() {
		return deleted;
	}

	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}

}
