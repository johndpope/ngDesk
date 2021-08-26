package com.ngdesk.sam.modules.dao;

import org.springframework.data.mongodb.core.mapping.Field;

public class DataType {
	@Field("DISPLAY")
	private String display;

	@Field("BACKEND")
	private String backend;

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
