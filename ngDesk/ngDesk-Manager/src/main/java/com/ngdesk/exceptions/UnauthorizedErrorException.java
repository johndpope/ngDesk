package com.ngdesk.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class UnauthorizedErrorException extends RuntimeException {
	public UnauthorizedErrorException(String message) {
		super(message);
	}
}