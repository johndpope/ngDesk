package com.ngdesk.module.layout.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ngdesk.commons.annotations.CustomNotEmpty;
import com.ngdesk.commons.annotations.CustomNotNull;

public class ListLayout {

	@Field("LAYOUT_ID")
	@JsonProperty("LAYOUT_ID")
	private String layoutId;

	@Field("NAME")
	@JsonProperty("NAME")
	@CustomNotEmpty(message = "DAO_VARIABLE_REQUIRED", values = { "LAYOUT_NAME" })
	@CustomNotNull(message = "NOT_NULL", values = { "LAYOUT_NAME" })
	private String name;

	@Field("DESCRIPTION")
	@JsonProperty("DESCRIPTION")
	@CustomNotNull(message = "NOT_NULL", values = { "DESCRIPTION" })
	private String description;

	@Field("ID")
	@JsonProperty("ID")
	private String id = "";

	@Field("ROLE")
	@JsonProperty("ROLE")
	private String role;

	@Field("IS_DEFAULT")
	@JsonProperty("IS_DEFAULT")
	private Boolean isDefault;

	@Field("ORDER_BY")
	@JsonProperty("ORDER_BY")
	@CustomNotNull(message = "NOT_NULL", values = { "ORDER_BY" })
	private OrderBy orderBy;

	@Field("COLUMN_SHOW")
	@JsonProperty("COLUMN_SHOW")
	@CustomNotNull(message = "NOT_NULL", values = { "COLUMN_SHOW" })
	private Column columnShow;

	@Field("CONDITIONS")
	@JsonProperty("CONDITIONS")
	private List<Condition> conditions = new ArrayList<>();

	@JsonProperty("DATE_CREATED")
	@Field("DATE_CREATED")
	private Date dateCreated = new Date();

	@JsonProperty("DATE_UPDATED")
	@Field("DATE_UPDATED")
	private Date dateUpdated = new Date();

	@JsonProperty("CREATED_BY")
	@Field("CREATED_BY")
	private String createdBy;

	@JsonProperty("LAST_UPDATED_BY")
	@Field("LAST_UPDATED_BY")
	private String lastUpdatedBy;

	public ListLayout() {
		super();
	}

	public ListLayout(String layoutId, String name, String description, String id, String role, Boolean isDefault,
			OrderBy orderBy, Column columnShow, List<Condition> conditions, Date dateCreated, Date dateUpdated,
			String createdBy, String lastUpdatedBy) {

		super();
		this.layoutId = layoutId;
		this.name = name;
		this.description = description;
		this.id = id;
		this.role = role;
		this.isDefault = isDefault;
		this.orderBy = orderBy;
		this.columnShow = columnShow;
		this.conditions = conditions;
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

	public Boolean getIsDefault() {
		return isDefault;
	}

	public void setIsDefault(Boolean isDefault) {
		this.isDefault = isDefault;
	}

	public OrderBy getOrderBy() {
		return orderBy;
	}

	public void setOrderBy(OrderBy orderBy) {
		this.orderBy = orderBy;
	}

	public Column getColumnShow() {
		return columnShow;
	}

	public void setColumnShow(Column columnShow) {
		this.columnShow = columnShow;
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
