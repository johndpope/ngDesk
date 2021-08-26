package com.ngdesk.commons.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.ngdesk.commons.Global;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class NotFoundException extends RuntimeException {
	
	private String[] variables;
	
	public NotFoundException(String message, String[] vars) {
		super(message);
		this.variables = vars;
	}
	
	public String[] getVariables() {
		return this.variables;
	}
}
