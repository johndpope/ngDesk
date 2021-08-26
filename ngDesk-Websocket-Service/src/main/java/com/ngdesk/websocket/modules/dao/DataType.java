package com.ngdesk.websocket.modules.dao;

import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DataType {

	@JsonProperty("DISPLAY")
	@Field("DISPLAY")
	private String display;

	@JsonProperty("BACKEND")
	@Field("BACKEND")
	private String backend;

	public DataType() {

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
