package com.ngdesk.graphql.campaigns.dao;

public class Column {

	private String type;

	private ColumnSettings settings;

	private Double width;

	public Column() {
	}

	public Column(String type, ColumnSettings settings, Double width) {
		super();
		this.type = type;
		this.settings = settings;
		this.width = width;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public ColumnSettings getSettings() {
		return settings;
	}

	public void setSettings(ColumnSettings settings) {
		this.settings = settings;
	}

	public Double getWidth() {
		return width;
	}

	public void setWidth(Double width) {
		this.width = width;
	}

}
