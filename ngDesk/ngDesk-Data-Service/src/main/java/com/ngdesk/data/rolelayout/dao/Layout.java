package com.ngdesk.data.rolelayout.dao;

import java.util.List;

import javax.validation.Valid;

import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ngdesk.commons.annotations.CustomNotEmpty;
import com.ngdesk.data.modules.dao.Condition;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.AccessMode;

public class Layout {

	@JsonProperty("LIST_LAYOUT_ID")
	@Field("LIST_LAYOUT_ID")
	private String listLayoutId;

	@JsonProperty("NAME")
	@Field("NAME")
	private String name;

	@JsonProperty("COLUMN_SHOW")
	@Field("COLUMN_SHOW")
	private List<String> columnsShow;

	@JsonProperty("ORDER_BY")
	@Field("ORDER_BY")
	@Valid
	private OrderBy orderBy;

	@JsonProperty("IS_DEFAULT")
	@Field("IS_DEFAULT")
	private boolean defaultLayout;

	@JsonProperty("CONDITIONS")
	@Field("CONDITIONS")
	@Valid
	private List<Condition> conditions;

	public Layout(String listLayoutId, String name, List<String> columnsShow, @Valid OrderBy orderBy,
			boolean defaultLayout, @Valid List<Condition> conditions) {
		super();
		this.listLayoutId = listLayoutId;
		this.name = name;
		this.columnsShow = columnsShow;
		this.orderBy = orderBy;
		this.defaultLayout = defaultLayout;
		this.conditions = conditions;
	}

	public Layout() {
	}

	public String getListLayoutId() {
		return listLayoutId;
	}

	public void setListLayoutId(String listLayoutId) {
		this.listLayoutId = listLayoutId;
	}

	public List<String> getColumnsShow() {
		return columnsShow;
	}

	public void setColumnsShow(List<String> columnsShow) {
		this.columnsShow = columnsShow;
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

	public boolean isDefaultLayout() {
		return defaultLayout;
	}

	public void setDefaultLayout(boolean defaultLayout) {
		this.defaultLayout = defaultLayout;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
