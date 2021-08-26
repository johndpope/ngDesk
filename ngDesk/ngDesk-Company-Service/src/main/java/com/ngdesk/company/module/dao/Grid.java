package com.ngdesk.company.module.dao;

import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Grid {
	
	@Field("IS_EMPTY")
	@JsonProperty("IS_EMPTY")
	private boolean empty=true;
	
	@Field("HEIGHT")
	@JsonProperty("HEIGHT")
	private int height=10;
	
	@Field("WIDTH")
	@JsonProperty("WIDTH")
	private int width=25;
	
	@Field("FIELD_ID")
	@JsonProperty("FIELD_ID")
	private String fieldId="";
	
	@Field("SETTINGS")
	@JsonProperty("SETTINGS")
	private FieldSettings settings =new FieldSettings() ;

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
