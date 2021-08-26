package com.ngdesk.graphql.escalation.dao;

import java.util.Date;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Escalation {

	@Id
	@JsonProperty("ESCALATION_ID")
	private String esclalationId;

	@Field("NAME")
	@JsonProperty("NAME")
	private String name;

	@JsonProperty("DESCRIPTION")
	@Field("DESCRIPTION")
	private String description;

	@JsonProperty("RULES")
	@Field("RULES")
	private List<EscalationRule> rules;

	@JsonProperty("DATE_CREATED")
	@Field("DATE_CREATED")
	private Date dateCreated;

	@JsonProperty("DATE_UPDATED")
	@Field("DATE_UPDATED")
	private Date dateUpdated;

	@JsonProperty("LAST_UPDATED_BY")
	@Field("LAST_UPDATED_BY")
	private String lastUpdated;

	@JsonProperty("CREATED_BY")
	@Field("CREATED_BY")
	private String createdBy;

	public Escalation() {

	}

	public Escalation(String esclalationId, String name, String description, List<EscalationRule> rules,
			Date dateCreated, Date dateUpdated, String lastUpdated, String createdBy) {
		super();
		this.esclalationId = esclalationId;
		this.name = name;
		this.description = description;
		this.rules = rules;
		this.dateCreated = dateCreated;
		this.dateUpdated = dateUpdated;
		this.lastUpdated = lastUpdated;
		this.createdBy = createdBy;
	}

	public String getEsclalationId() {
		return esclalationId;
	}

	public void setEsclalationId(String esclalationId) {
		this.esclalationId = esclalationId;
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

}
