package com.ngdesk.workflow.dao;

import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;

public class StartEscalationNode extends Node {

	@JsonProperty("SUBJECT")
	@Field("SUBJECT")
	private String subject;

	@JsonProperty("BODY")
	@Field("BODY")
	private String body;

	@JsonProperty("ESCALATION_ID")
	@Field("ESCALATION_ID")
	private String escalationId;

	public StartEscalationNode(String subject, String body, String escalationId) {
		this.subject = subject;
		this.body = body;
		this.escalationId = escalationId;
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

	public String getEscalationId() {
		return escalationId;
	}

	public void setEscalationId(String escalationId) {
		this.escalationId = escalationId;
	}

	public StartEscalationNode() {
	}

}
