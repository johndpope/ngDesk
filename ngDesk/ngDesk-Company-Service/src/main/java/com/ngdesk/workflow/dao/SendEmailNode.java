package com.ngdesk.workflow.dao;

import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SendEmailNode extends Node {

	@JsonProperty("TO")
	@Field("TO")
	private String to;

	@JsonProperty("FROM")
	@Field("FROM")
	private String from;

	@JsonProperty("SUBJECT")
	@Field("SUBJECT")
	private String subject;

	@JsonProperty("BODY")
	@Field("BODY")
	private String body;

	public SendEmailNode() {

	}

	public SendEmailNode(String to, String from, String subject, String body) {
		super();
		this.to = to;
		this.from = from;
		this.subject = subject;
		this.body = body;
	}

	public String getTo() {
		return to;
	}

	public void setTo(String to) {
		this.to = to;
	}

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

}
