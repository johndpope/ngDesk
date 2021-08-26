package com.ngdesk.graphql.modules.dao;

import java.util.Date;
import java.util.List;

import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ListMobileLayout {

	@Field("LAYOUT_ID")
	private String layoutId;

	@Field("NAME")
	private String name;

	@Field("DESCRIPTION")
	private String description;

	@Field("ID")
	private String id;

	@Field("ROLE")
	private String role;

	@Field("ORDER_BY")
	private OrderBy orderBy;

	@Field("FIELDS")
	private List<String> fields;

	@Field("CONDITIONS")
	private List<Condition> conditions;

	@Field("IS_DEFAULT")
	private boolean isDefault;

	@Field("DATE_CREATED")
	private Date dateCreated;

	@Field("DATE_UPDATED")
	private Date dateUpdated;

	@Field("CREATED_BY")
	private String createdBy;

	@JsonProperty("LAST_UPDATED_BY")
	@Field("LAST_UPDATED_BY")
	private String lastUpdatedBy;

	public ListMobileLayout() {
		super();
	}

	public ListMobileLayout(String layoutId, String name, String description, String id, String role, OrderBy orderBy,
			List<String> fields, List<Condition> conditions, boolean isDefault, Date dateCreated, Date dateUpdated,
			String createdBy, String lastUpdatedBy) {
		super();
		this.layoutId = layoutId;
		this.name = name;
		this.description = description;
		this.id = id;
		this.role = role;
		this.orderBy = orderBy;
		this.fields = fields;
		this.conditions = conditions;
		this.isDefault = isDefault;
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

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public OrderBy getOrderBy() {
		return orderBy;
	}

	public void setOrderBy(OrderBy orderBy) {
		this.orderBy = orderBy;
	}

	public List<String> getFields() {
		return fields;
	}

	public void setFields(List<String> fields) {
		this.fields = fields;
	}

	public List<Condition> getConditions() {
		return conditions;
	}

	public void setConditions(List<Condition> conditions) {
		this.conditions = conditions;
	}

	public boolean isDefault() {
		return isDefault;
	}

	public void setDefault(boolean isDefault) {
		this.isDefault = isDefault;
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
