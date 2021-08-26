package com.ngdesk.graphql.chat.channel.dao;

import org.springframework.data.mongodb.core.mapping.Field;

public class Values {

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
