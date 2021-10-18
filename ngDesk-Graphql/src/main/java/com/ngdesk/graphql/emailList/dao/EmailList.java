package com.ngdesk.graphql.emailList.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EmailList {

	@Id
	private String emailListId;

	@Field("NAME")
	private String name;

	@Field("DESCRIPTION")
	private String description;

	@Field("CONDITIONS")
	private List<EmailLIstCondition> conditions = new ArrayList<>();

	@Field("DATE_CREATED")
	private Date dateCreated;

	@Field("DATE_UPDATED")
	private Date dateUpdated;

	@Field("LAST_UPDATED_BY")
	private String lastUpdatedBy;

	@Field("CREATED_BY")
	private String createdBy;

	public EmailList() {

	}

	public EmailList(String emailListId, String name, String description, List<EmailLIstCondition> conditions,
			Date dateCreated, Date dateUpdated, String lastUpdatedBy, String createdBy) {
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

	public List<EmailLIstCondition> getConditions() {
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

	public void setConditions(List<EmailLIstCondition> conditions) {
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
