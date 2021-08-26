package com.ngdesk.data.sla.dao;

import java.util.Date;
import java.util.List;

import org.springframework.data.annotation.Id;

public class SLA {

	@Id
	private String slaId;

	private String name;

	private String companyId;

	private String moduleId;

	private List<SLAConditions> conditions;

	private Violation violation;

	private int slaExpiry;

	private SLABusinessRules businessRules;

	private Boolean isRestricted;

	private boolean deleted;

	private Date dateCreated;

	private Date dateUpdated;

	public SLA() {

	}

	public SLA(String slaId, String name, String companyId, String moduleId, List<SLAConditions> conditions,
			Violation violation, int slaExpiry, SLABusinessRules businessRules, Boolean isRestricted, boolean deleted,
			Date dateCreated, Date dateUpdated) {
		super();
		this.slaId = slaId;
		this.name = name;
		this.companyId = companyId;
		this.moduleId = moduleId;
		this.conditions = conditions;
		this.violation = violation;
		this.slaExpiry = slaExpiry;
		this.businessRules = businessRules;
		this.isRestricted = isRestricted;
		this.deleted = deleted;
		this.dateCreated = dateCreated;
		this.dateUpdated = dateUpdated;
	}

	public String getSlaId() {
		return slaId;
	}

	public void setSlaId(String slaId) {
		this.slaId = slaId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
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

	public SLABusinessRules getBusinessRules() {
		return businessRules;
	}

	public void setBusinessRules(SLABusinessRules businessRules) {
		this.businessRules = businessRules;
	}

	public Boolean getIsRestricted() {
		return isRestricted;
	}

	public void setIsRestricted(Boolean isRestricted) {
		this.isRestricted = isRestricted;
	}

	public boolean isDeleted() {
		return deleted;
	}

	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
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

}
