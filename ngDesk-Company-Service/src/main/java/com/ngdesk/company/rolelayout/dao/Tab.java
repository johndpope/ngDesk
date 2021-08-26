package com.ngdesk.company.rolelayout.dao;

import java.util.List;

import javax.validation.Valid;

import com.ngdesk.commons.annotations.CustomNotEmpty;

import io.swagger.v3.oas.annotations.media.Schema;

public class Tab {

	private String tabId;

	@Schema(required = true, description = "Module id  of the layout")
	@CustomNotEmpty(message = "DAO_VARIABLE_REQUIRED", values = { "MODULE_ID_FOR_LAYOUT" })
	private String module;

	@Schema(description = "Field ids in current module which has to be shown in the layout", required = true)
	@CustomNotEmpty(message = "DAO_VARIABLE_REQUIRED", values = { "COLUMNS_SHOW" })
	private List<String> columnsShow;

	@Valid
	private OrderBy orderBy;

	@Schema(description = "Conditions to show the data in list layout", required = true)
	@Valid
	private List<Condition> conditions;

	public Tab() {
	}

	public Tab(String tabId, String module, List<String> columnsShow, @Valid OrderBy orderBy,
			@Valid List<Condition> conditions) {
		super();
		this.tabId = tabId;
		this.module = module;
		this.columnsShow = columnsShow;
		this.orderBy = orderBy;
		this.conditions = conditions;
	}

	public String getTabId() {
		return tabId;
	}

	public void setTabId(String tabId) {
		this.tabId = tabId;
	}

	public String getModule() {
		return module;
	}

	public void setModule(String module) {
		this.module = module;
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

}
