package com.ngdesk.modules.detail.layouts;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TitleBarGrid {

	@JsonProperty("FIELD_ID")
	private String fieldId;

	@JsonProperty("SETTINGS")
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
