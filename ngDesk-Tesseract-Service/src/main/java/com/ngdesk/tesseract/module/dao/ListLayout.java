package com.ngdesk.tesseract.module.dao;

import java.util.List;

import javax.validation.Valid;

import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ListLayout {

	@JsonProperty("LAYOUT_ID")
	@Field("LAYOUT_ID")
	private String listLayoutId;

	@JsonProperty("NAME")
	@Field("NAME")
	private String name;

	@JsonProperty("DESCRIPTION")
	@Field("DESCRIPTION")
	private String description;

	@JsonProperty("ROLE")
	@Field("ROLE")
	private String role;

	@JsonProperty("COLUMN_SHOW")
	@Field("COLUMN_SHOW")
	private Column showColumns;

	@JsonProperty("ORDER_BY")
	@Field("ORDER_BY")
	private OrderBy orderBy;

	@JsonProperty("CONDITIONS")
	@Field("CONDITIONS")
	private List<Condition> conditions;

	public ListLayout() {

	}

	public ListLayout(String listLayoutId, String name, String description, String role, Column showColumns,
			@Valid OrderBy orderBy, List<Condition> conditions) {
		super();
		this.listLayoutId = listLayoutId;
		this.name = name;
		this.description = description;
		this.role = role;
		this.showColumns = showColumns;
		this.orderBy = orderBy;
		this.conditions = conditions;
	}

	public String getListLayoutId() {
		return listLayoutId;
	}

	public void setListLayoutId(String listLayoutId) {
		this.listLayoutId = listLayoutId;
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

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public Column getShowColumns() {
		return showColumns;
	}

	public void setShowColumns(Column showColumns) {
		this.showColumns = showColumns;
	}

	public OrderBy getOrderBy() {
		return orderBy;
	}

	public void setOrderBy(OrderBy orderBy) {
		this.orderBy = orderBy;
	}

	public List<Condition> getConditions() {
		return conditions;
	}

	public void setConditions(List<Condition> conditions) {
		this.conditions = conditions;
	}

}
