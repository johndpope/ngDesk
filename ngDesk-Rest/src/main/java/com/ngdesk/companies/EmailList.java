package com.ngdesk.companies;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.springframework.data.annotation.Id;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ngdesk.modules.rules.Condition;

public class EmailList {

	@JsonProperty("EMAIL_LIST_ID")
	private String emailListId;

	@JsonProperty("NAME")
	@NotNull(message = "EMAIL_LIST_NAME_NOT_NULL")
	private String name;

	@JsonProperty("DESCRIPTION")
	private String description;

	@JsonProperty("CONDITIONS")
	@Valid
	private List<Condition> conditions;

	@JsonProperty("DATE_CREATED")
	private Date dateCreated;

	@JsonProperty("DATE_UPDATED")
	private Date dateUpdated;

	@JsonProperty("LAST_UPDATED_BY")
	private String lastUpdatedBy;

	@JsonProperty("CREATED_BY")
	private String createdBy;

	public EmailList() {

	}

	public EmailList(String emailListId, @NotNull(message = "EMAIL_LIST_NAME_NOT_NULL") String name, String description,
			@Valid List<Condition> conditions, Date dateCreated, Date dateUpdated, String lastUpdatedBy,
			String createdBy) {
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

	public void setEmailListId(String emailListId) {
		this.emailListId = emailListId;
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

	public Date getDateCreated() {
		return dateCreated;
	}

	public Date getDateUpdated() {
		return dateUpdated;
	}

	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
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