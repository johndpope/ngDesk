package com.ngdesk.graphql.campaigns.dao;

import org.springframework.data.mongodb.core.mapping.Field;

public class Column {
	@Field("TYPE")
	private String type;
	@Field("SETTINGS")
	private ColumnSettings settings;
	@Field("WIDTH")
	private Float width;

	public Column() {
	}

	public Column(String type, ColumnSettings settings, Float width) {
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

	public Float getWidth() {
		return width;
	}

	public void setWidth(Float width) {
		this.width = width;
	}

}
