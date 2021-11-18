package com.ngdesk.graphql.campaigns.dao;

import java.util.List;

import org.springframework.data.mongodb.core.mapping.Field;

public class Row {
	@Field("COLUMNS")
	private List<Column> columns;
	@Field("SETTINGS")
	private RowSettings settings;

	public Row() {
	}

	public Row(List<Column> columns, RowSettings settings) {
		super();
		this.columns = columns;
		this.settings = settings;
	}

	public List<Column> getColumns() {
		return columns;
	}

	public void setColumns(List<Column> columns) {
		this.columns = columns;
	}

	public RowSettings getSettings() {
		return settings;
	}

	public void setSettings(RowSettings settings) {
		this.settings = settings;
	}

}
