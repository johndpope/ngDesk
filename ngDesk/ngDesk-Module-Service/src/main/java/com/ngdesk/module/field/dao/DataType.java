package com.ngdesk.module.field.dao;

import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ngdesk.commons.annotations.CustomNotEmpty;

import io.swagger.v3.oas.annotations.media.Schema;

public class DataType {

	@Schema(description = "Display of the data type", required = true)
	@Field("DISPLAY")
	@JsonProperty("DISPLAY")
	@CustomNotEmpty(message = "DAO_VARIABLE_REQUIRED", values = { "DISPLAY_DATA_TYPE" })
	String display;

	@Schema(description = "Backend data type", required = true)
	@Field("BACKEND")
	@JsonProperty("BACKEND")
	@CustomNotEmpty(message = "DAO_VARIABLE_REQUIRED", values = { "BACKEND_DATA_TYPE" })
	String backend;

	public DataType() {
		super();
	}

	public DataType(String display, String backend) {
		super();
		this.display = display;
		this.backend = backend;
	}

	public String getDisplay() {
		return display;
	}

	public void setDisplay(String display) {
		this.display = display;
	}

	public String getBackend() {
		return backend;
	}

	public void setBackend(String backend) {
		this.backend = backend;
	}

}

