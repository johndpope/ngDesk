package com.ngdesk.graphql.escalation.dao;

import java.util.Date;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Escalation {

	@Id
	private String escalationId;

	@Field("NAME")
	private String name;

	@Field("DESCRIPTION")
	private String description;

	@Field("RULES")
	private List<EscalationRule> rules;

	@Field("DATE_CREATED")
	private Date dateCreated;

	@Field("DATE_UPDATED")
	private Date dateUpdated;

	@Field("LAST_UPDATED_BY")
	private String lastUpdatedBy;

	@Field("CREATED_BY")
	private String createdBy;

	public Escalation() {

	}

	public Escalation(String escalationId, String name, String description, List<EscalationRule> rules,
			Date dateCreated, Date dateUpdated, String lastUpdatedBy, String createdBy) {
		super();
		this.escalationId = escalationId;
		this.name = name;
		this.description = description;
		this.rules = rules;
		this.dateCreated = dateCreated;
		this.dateUpdated = dateUpdated;
		this.lastUpdatedBy = lastUpdatedBy;
		this.createdBy = createdBy;
	}

	public String getEsclalationId() {
		return escalationId;
	}

	public void setEsclalationId(String escalationId) {
		this.escalationId = escalationId;
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

	public List<EscalationRule> getRules() {
		return rules;
	}

	public void setRules(List<EscalationRule> rules) {
		this.rules = rules;
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

}
