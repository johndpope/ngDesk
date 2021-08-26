package com.ngdesk.graphql.role.layout.dao;

import java.util.List;

public class Tab {

	private String tabId;

	private String module;

	private List<String> columnsShow;

	private OrderBy orderBy;

	private List<RoleLayoutCondition> conditions;

	public Tab() {
	}

	public Tab(String tabId, String module, List<String> columnsShow, OrderBy orderBy,
			List<RoleLayoutCondition> conditions) {
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

	public List<RoleLayoutCondition> getConditions() {
		return conditions;
	}

	public void setConditions(List<RoleLayoutCondition> conditions) {
		this.conditions = conditions;
	}

}