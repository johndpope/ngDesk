package com.ngdesk.commons.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class BadRequestException extends RuntimeException {
	
	private String[] variables;
	
	public BadRequestException(String message, String[] variables) {
		super(message);
		this.variables = variables;
	}
	
	public String[] getVariables() {
		return this.variables;
	}
}
