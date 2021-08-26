package com.ngdesk.campaigns;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Row {
	@JsonProperty("COLUMNS")
	private List<Column> columns;
	
	@JsonProperty("SETTINGS")
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
