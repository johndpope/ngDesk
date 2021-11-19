package com.ngdesk.graphql.controllers;

import java.util.List;

import com.ngdesk.graphql.modules.dao.Condition;

public class DataInput {
	private String query;

	private List<Condition> conditions;

	public DataInput() {

	}

	public DataInput(String query, List<Condition> conditions) {
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

	public List<Condition> getConditions() {
		return conditions;
	}

	public void setConditions(List<Condition> conditions) {
		this.conditions = conditions;
	}

}
