package com.ngdesk.company.module.dao;

import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TitleBarGrid {
	

	@JsonProperty("FIELD_ID")
	@Field("FIELD_ID")
	private String fieldId;


	@JsonProperty("SETTINGS")
	@Field("SETTINGS")
	private FieldSettings settings;

	public TitleBarGrid(String fieldId, FieldSettings settings) {
		super();
		this.fieldId = fieldId;
		this.settings = settings;
	}

	public TitleBarGrid() {
	}

	public String getFieldId() {
		return fieldId;
	}

	public void setFieldId(String fieldId) {
		this.fieldId = fieldId;
	}

	public FieldSettings getSettings() {
		return settings;
	}

	public void setSettings(FieldSettings settings) {
		this.settings = settings;
	}

}
