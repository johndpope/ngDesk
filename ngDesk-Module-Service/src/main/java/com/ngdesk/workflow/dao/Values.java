package com.ngdesk.workflow.dao;

import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Values {

	@JsonProperty("MESSAGE")
	@Field("MESSAGE")
	private String message;

	public Values() {

	}

	public Values(String message) {
		super();
		this.message = message;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

}
