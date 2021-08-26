package com.ngdesk.modules.detail.layouts;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Grid {

	@JsonProperty("IS_EMPTY")
	private boolean empty;

	@JsonProperty("HEIGHT")
	private int height;

	@JsonProperty("WIDTH")
	private int width;

	@JsonProperty("FIELD_ID")
	private String fieldId;

	@JsonProperty("SETTINGS")
	private FieldSettings settings;

	public Grid() {

	}

	public Grid(boolean empty, int height, int width, String fieldId, FieldSettings settings) {
		super();
		this.empty = empty;
		this.height = height;
		this.width = width;
		this.fieldId = fieldId;
		this.settings = settings;
	}

	public boolean isEmpty() {
		return empty;
	}

	public void setEmpty(boolean empty) {
		this.empty = empty;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
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