package com.ngdesk.graphql.form.dao;

public class Grid {

	private boolean empty;

	private int height;

	private int width;

	private String fieldId;

	private FieldSettings settings = new FieldSettings();

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
