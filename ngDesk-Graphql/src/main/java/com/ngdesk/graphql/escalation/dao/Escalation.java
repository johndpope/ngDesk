package com.ngdesk.graphql.escalation.dao;

import java.util.Date;
import java.util.List;

import org.springframework.data.annotation.Id;

public class Escalation {

	@Id
	private String esclalationId;

	private String name;

	private String description;

	private List<EscalationRule> rules;

	private Date dateCreated;

	private Date dateUpdated;

	private String lastUpdated;

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
