package com.ngdesk.company.dao;

import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;

public class InviteMessage {

	@JsonProperty("SUBJECT")
	@Field("SUBJECT")
	private String subject;

	@JsonProperty("MESSAGE_1")
	@Field("MESSAGE_1")
	private String message1;

	@JsonProperty("MESSAGE_2")
	@Field("MESSAGE_2")
	private String message2;
	
	@JsonProperty("FROM_ADDRESS")
	@Field("FROM_ADDRESS")
	private String fromAddress;

	public InviteMessage() {
		super();
	}

	public InviteMessage(String subject, String message1, String message2, String fromAddress) {
		super();
		this.subject = subject;
		this.message1 = message1;
		this.message2 = message2;
		this.fromAddress = fromAddress;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getMessage1() {
		return message1;
	}

	public void setMessage1(String message1) {
		this.message1 = message1;
	}

	public String getMessage2() {
		return message2;
	}

	public void setMessage2(String message2) {
		this.message2 = message2;
	}

	public String getFromAddress() {
		return fromAddress;
	}

	public void setFromAddress(String fromAddress) {
		this.fromAddress = fromAddress;
	}

	
}
