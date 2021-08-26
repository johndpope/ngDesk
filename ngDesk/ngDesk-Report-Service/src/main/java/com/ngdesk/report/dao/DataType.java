package com.ngdesk.report.dao;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DataType {

	@JsonProperty("DISPLAY")
	@NotNull(message = "DISPLAY_NOT_NULL")
	@Size(min = 1, message = "DISPLAY_EMPTY")
	public String display;

	@JsonProperty("BACKEND")
	@NotNull(message = "BACK_END_NOT_NULL")
	@Size(min = 1, message = "BACK_END_EMPTY")
	public String backend;

	public DataType() {

	}

	public DataType(@NotNull(message = "DISPLAY_NOT_NULL") @Size(min = 1, message = "DISPLAY_EMPTY") String display,
			@NotNull(message = "BACK_END_NOT_NULL") @Size(min = 1, message = "BACK_END_EMPTY") String backend) {
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
