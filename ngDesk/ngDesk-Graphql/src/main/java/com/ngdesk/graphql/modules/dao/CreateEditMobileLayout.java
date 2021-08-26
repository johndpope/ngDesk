package com.ngdesk.graphql.modules.dao;

import java.util.Date;
import java.util.List;

import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ngdesk.commons.annotations.CustomNotEmpty;
import com.ngdesk.commons.annotations.CustomNotNull;

public class CreateEditMobileLayout {

	@Field("LAYOUT_ID")
	private String layoutId;

	@Field("NAME")
	private String name;

	@Field("DESCRIPTION")
	private String description;

	@Field("FIELDS")
	private List<String> fields;

	@Field("ROLE")
	private String role;

	@Field("DATE_CREATED")
	private Date dateCreated = new Date();

	@Field("DATE_UPDATED")
	private Date dateUpdated = new Date();

	@Field("CREATED_BY")
	private String createdBy;

	@Field("LAST_UPDATED_BY")
	private String lastUpdatedBy;

	public CreateEditMobileLayout() {
		super();
	}

	public CreateEditMobileLayout(String layoutId, String name, String description, List<String> fields, String role,
			Date dateCreated, Date dateUpdated, String createdBy, String lastUpdatedBy) {
		super();
		this.layoutId = layoutId;
		this.name = name;
		this.description = description;
		this.fields = fields;
		this.role = role;
		this.dateCreated = dateCreated;
		this.dateUpdated = dateUpdated;
		this.createdBy = createdBy;
		this.lastUpdatedBy = lastUpdatedBy;
	}

	public String getLayoutId() {
		return layoutId;
	}

	public void setLayoutId(String layoutId) {
		this.layoutId = layoutId;
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

	public List<String> getFields() {
		return fields;
	}

	public void setFields(List<String> fields) {
		this.fields = fields;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
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

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public String getLastUpdatedBy() {
		return lastUpdatedBy;
	}

	public void setLastUpdatedBy(String lastUpdatedBy) {
		this.lastUpdatedBy = lastUpdatedBy;
	}

}
