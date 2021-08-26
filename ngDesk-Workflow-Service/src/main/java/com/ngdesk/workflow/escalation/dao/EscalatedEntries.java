package com.ngdesk.workflow.escalation.dao;

import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;

public class EscalatedEntries {

	@JsonProperty("ESCALATION_ID")
	@Field("ESCALATION_ID")
	private String escalationId;

	@JsonProperty("ENTRY_ID")
	@Field("ENTRY_ID")
	private String entryId;

	@JsonProperty("MODULE_ID")
	@Field("MODULE_ID")
	private String moduleId;

	@JsonProperty("BODY")
	@Field("BODY")
	private String body;

	@JsonProperty("SUBJECT")
	@Field("SUBJECT")
	private String subject;

	public EscalatedEntries(String escalationId, String entryId, String moduleId, String body, String subject) {
		this.escalationId = escalationId;
		this.entryId = entryId;
		this.moduleId = moduleId;
		this.body = body;
		this.subject = subject;
	}

	public EscalatedEntries() {
	}

	public String getEscalationId() {
		return escalationId;
	}

	public void setEscalationId(String escalationId) {
		this.escalationId = escalationId;
	}

	public String getEntryId() {
		return entryId;
	}

	public void setEntryId(String entryId) {
		this.entryId = entryId;
	}

	public String getModuleId() {
		return moduleId;
	}

	public void setModuleId(String moduleId) {
		this.moduleId = moduleId;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

}
