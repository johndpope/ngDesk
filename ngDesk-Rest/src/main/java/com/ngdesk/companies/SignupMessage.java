package com.ngdesk.companies;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SignupMessage {
	@JsonProperty("MESSAGE")
	@NotNull(message = "MESSAGE_NOT_NULL")
	@Size(min = 1, message = "MESSAGE_NOT_EMPTY")
	private String message;

	@JsonProperty("SUBJECT")
	@NotNull(message = "SUBJECT_NOT_NULL")
	@Size(min = 1, message = "SUBJECT_NOT_EMPTY")
	private String subject;

	@JsonProperty("FROM_ADDRESS")
	private String from;

	public SignupMessage() {
	}

	public SignupMessage(
			@NotNull(message = "MESSAGE_NOT_NULL") @Size(min = 1, message = "MESSAGE_NOT_EMPTY") String message,
			@NotNull(message = "SUBJECT_NOT_NULL") @Size(min = 1, message = "SUBJECT_NOT_EMPTY") String subject,
			String from) {
		super();
		this.message = message;
		this.subject = subject;
		this.from = from;
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

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

}
