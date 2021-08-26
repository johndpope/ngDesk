package com.ngdesk.module.form.dao;

import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.AccessMode;

public class Grid {

	@Schema(description = "Empty")
	private boolean empty = true;

	@Schema(description = "Height")
	private int height = 10;

	@Schema(description = "Width")
	private int width = 25;

	@Schema(description = "Field Id", required = false)
	private String fieldId = "";

	@Schema(description = "Field Settings", required = false)
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
