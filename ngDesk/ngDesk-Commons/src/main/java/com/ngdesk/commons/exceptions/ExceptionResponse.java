package com.ngdesk.commons.exceptions;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ExceptionResponse {

	@JsonProperty("ERROR")
	private String message;

	public ExceptionResponse(String message) {
		super();
		this.message = message;
	}

	public String getMessage() {
		return message;
	}

}
