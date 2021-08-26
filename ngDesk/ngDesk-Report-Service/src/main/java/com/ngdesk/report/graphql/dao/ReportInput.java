package com.ngdesk.report.graphql.dao;

import java.util.List;

public class ReportInput {

	private String query;

	private List<RoleLayoutCondition> conditions;

	private String fileName;

	private List<String> fieldNames;

	private List<String> emailIds;

	public ReportInput() {

	}

	public ReportInput(String query, List<RoleLayoutCondition> conditions, String fileName, List<String> fieldNames,
			List<String> emailIds) {
		super();
		this.query = query;
		this.conditions = conditions;
		this.fileName = fileName;
		this.fieldNames = fieldNames;
		this.emailIds = emailIds;
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

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public List<String> getFieldNames() {
		return fieldNames;
	}

	public void setFieldNames(List<String> fieldNames) {
		this.fieldNames = fieldNames;
	}

	public List<String> getEmailIds() {
		return emailIds;
	}

	public void setEmailIds(List<String> emailIds) {
		this.emailIds = emailIds;
	}

}
