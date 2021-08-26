package com.ngdesk.commons.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ErrorModel {

	@JsonProperty("ERROR")
	private String error;

	public ErrorModel() {
		super();
	}

	public ErrorModel(String error) {
		super();
		this.error = error;
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}

}
