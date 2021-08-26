package com.ngdesk.campaigns;

import java.util.List;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Column {
	@JsonProperty("TYPE")
	private String type;
	
	@JsonProperty("SETTINGS")
	private ColumnSettings settings;
	
	@JsonProperty("WIDTH")
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
