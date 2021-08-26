package com.ngdesk.companies;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonProperty;

public class InviteMessage {
	
	@JsonProperty("MESSAGE_1")
	@NotNull(message = "MESSAGE_NOT_NULL")
	@Size(min = 1, message = "MESSAGE_NOT_EMPTY")
	private String firstMessage;

	@JsonProperty("MESSAGE_2")
	@NotNull(message = "MESSAGE_NOT_NULL")
	@Size(min = 1, message = "MESSAGE_NOT_EMPTY")
	private String secondMessage;

	@JsonProperty("SUBJECT")
	@NotNull(message = "SUBJECT_NOT_NULL")
	@Size(min = 1, message = "SUBJECT_NOT_EMPTY")
	private String subject;

	@JsonProperty("FROM_ADDRESS")
	private String from;

	public InviteMessage() {
	}

	public InviteMessage(
			@NotNull(message = "MESSAGE_NOT_NULL") @Size(min = 1, message = "MESSAGE_NOT_EMPTY") String firstMessage,
			@NotNull(message = "MESSAGE_NOT_NULL") @Size(min = 1, message = "MESSAGE_NOT_EMPTY") String secondMessage,
			@NotNull(message = "SUBJECT_NOT_NULL") @Size(min = 1, message = "SUBJECT_NOT_EMPTY") String subject,
			String from) {
		super();
		this.firstMessage = firstMessage;
		this.secondMessage = secondMessage;
		this.subject = subject;
		this.from = from;
	}

	public String getFirstMessage() {
		return firstMessage;
	}

	public void setFirstMessage(String firstMessage) {
		this.firstMessage = firstMessage;
	}

	public String getSecondMessage() {
		return secondMessage;
	}

	public void setSecondMessage(String secondMessage) {
		this.secondMessage = secondMessage;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

}
