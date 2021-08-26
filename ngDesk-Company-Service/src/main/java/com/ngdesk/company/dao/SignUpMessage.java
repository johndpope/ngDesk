package com.ngdesk.company.dao;

import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SignUpMessage {

	@JsonProperty("MESSAGE")
	@Field("MESSAGE")
	private String message = "Welcome to ngDesk";

	@JsonProperty("SUBJECT")
	@Field("SUBJECT")
	private String subject = "Welcome to ngDesk";

	@JsonProperty("FROM_ADDRESS")
	@Field("FROM_ADDRESS")
	private String fromAddress;

	public SignUpMessage() {
		super();
	}

	public SignUpMessage(String message, String subject, String fromAddress) {
		super();
		this.message = message;
		this.subject = subject;
		this.fromAddress = fromAddress;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getFromAddress() {
		return fromAddress;
	}

	public void setFromAddress(String fromAddress) {
		this.fromAddress = fromAddress;
	}

}
