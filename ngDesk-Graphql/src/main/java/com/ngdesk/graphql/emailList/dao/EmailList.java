package com.ngdesk.graphql.emailList.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.ngdesk.graphql.modules.dao.Condition;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EmailList {

	private String emailListId;

	private String name;

	private String description;

	private List<Condition> conditions = new ArrayList<>();

	private Date dateCreated;

	private Date dateUpdated;

	private String lastUpdatedBy;

	private String createdBy;

	public EmailList() {

	}

	public EmailList(String emailListId, String name, String description, List<Condition> conditions, Date dateCreated,
			Date dateUpdated, String lastUpdatedBy, String createdBy) {
		super();
		this.emailListId = emailListId;
		this.name = name;
		this.description = description;
		this.conditions = conditions;
		this.dateCreated = dateCreated;
		this.dateUpdated = dateUpdated;
		this.lastUpdatedBy = lastUpdatedBy;
		this.createdBy = createdBy;
	}

	public String getEmailListId() {
		return emailListId;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public List<Condition> getConditions() {
		return conditions;
	}

	public Date getDateCreated() {
		return dateCreated;
	}

	public Date getDateUpdated() {
		return dateUpdated;
	}

	public String getLastUpdatedBy() {
		return lastUpdatedBy;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setEmailListId(String emailListId) {
		this.emailListId = emailListId;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setConditions(List<Condition> conditions) {
		this.conditions = conditions;
	}

	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}

	public void setDateUpdated(Date dateUpdated) {
		this.dateUpdated = dateUpdated;
	}

	public void setLastUpdatedBy(String lastUpdatedBy) {
		this.lastUpdatedBy = lastUpdatedBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

}
