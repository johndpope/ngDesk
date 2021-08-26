package com.ngdesk.auth.company.dao;

import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ForgotPasswordMessage {

	@JsonProperty("FROM_ADDRESS")
	@Field("FROM_ADDRESS")
	private String fromAddress;

	@JsonProperty("SUBJECT")
	@Field("SUBJECT")
	private String subject;

	@JsonProperty("MESSAGE_2")
	@Field("MESSAGE_2")
	private String message2;

	@JsonProperty("MESSAGE_1")
	@Field("MESSAGE_1")
	private String message1;

	public ForgotPasswordMessage() {

	}

	public ForgotPasswordMessage(String fromAddress, String subject, String message2, String message1) {
		super();
		this.fromAddress = fromAddress;
		this.subject = subject;
		this.message2 = message2;
		this.message1 = message1;
	}

	public String getFromAddress() {
		return fromAddress;
	}

	public void setFromAddress(String fromAddress) {
		this.fromAddress = fromAddress;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getMessage2() {
		return message2;
	}

	public void setMessage2(String message2) {
		this.message2 = message2;
	}

	public String getMessage1() {
		return message1;
	}

	public void setMessage1(String message1) {
		this.message1 = message1;
	}

}
