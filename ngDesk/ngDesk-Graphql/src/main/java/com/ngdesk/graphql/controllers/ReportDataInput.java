package com.ngdesk.graphql.controllers;

import java.util.List;

import com.ngdesk.graphql.role.layout.dao.RoleLayoutCondition;

public class ReportDataInput {
	private String query;

	private List<RoleLayoutCondition> conditions;

	public ReportDataInput() {

	}

	public ReportDataInput(String query, List<RoleLayoutCondition> conditions) {
		super();
		this.query = query;
		this.conditions = conditions;
	}

	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}

	public List<RoleLayoutCondition> getConditions() {
		return conditions;
	}

	public void setConditions(List<RoleLayoutCondition> conditions) {
		this.conditions = conditions;
	}

}
