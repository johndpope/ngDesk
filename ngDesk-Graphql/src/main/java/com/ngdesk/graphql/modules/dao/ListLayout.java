package com.ngdesk.graphql.modules.dao;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ngdesk.commons.annotations.CustomNotEmpty;
import com.ngdesk.commons.annotations.CustomNotNull;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ListLayout {

	@Field("LAYOUT_ID")
	private String layoutId;

	@Field("NAME")
	private String name;

	@Field("DESCRIPTION")
	private String description;

	@Field("ID")
	private String id = "";

	@Field("ROLE")
	private String role;

	@Field("IS_DEFAULT")
	private Boolean isDefault;

	@Field("ORDER_BY")
	private OrderBy orderBy;

	@Field("COLUMN_SHOW")
	private Column columnShow;

	@Field("CONDITIONS")
	private List<Condition> conditions = new ArrayList<>();

	@Field("CREATED_BY")
	private String createdBy;

	@Field("LAST_UPDATED_BY")
	private String lastUpdatedBy;

	public ListLayout() {
	}

	public ListLayout(String layoutId, String name, String description, String id, String role, Boolean isDefault,
			OrderBy orderBy, Column columnShow, List<Condition> conditions, String createdBy, String lastUpdatedBy) {
		this.layoutId = layoutId;
		this.name = name;
		this.description = description;
		this.id = id;
		this.role = role;
		this.isDefault = isDefault;
		this.orderBy = orderBy;
		this.columnShow = columnShow;
		this.conditions = conditions;
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
