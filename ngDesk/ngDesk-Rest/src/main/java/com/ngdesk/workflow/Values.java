package com.ngdesk.workflow;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.URL;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(Include.NON_NULL)
public class Values {
	@JsonProperty("TO")
	@Size(min = 1, message = "NODE_VALUE_TO_EMPTY")
	private String to;

	@JsonProperty("FROM")
	@Email(message = "INVALID_FROM_EMAILADDRESS")
	@Size(min = 1, message = "NODE_VALUE_FROM_EMPTY")
	private String from;

	@JsonProperty("SUBJECT")
	@Size(min = 1, message = "NODE_VALUE_SUBJECT_EMPTY")
	@Size(max = 255, message = "NODE_VALUE_SUBJECT_MAX")
	private String subject;

	@JsonProperty("BODY")
	// @Size(min=1, message="NODE_VALUE_BODY_EMPTY")
	@Size(max = 2621400, message = "NODE_VALUE_BODY_MAX")
	private String body;

	@JsonProperty("MODULE")
	@Size(min = 1, message = "NODE_VALUE_MODULE_ID_EMPTY")
	private String moduleId;

	@JsonProperty("ENTRY_ID")
	@Size(min = 1, message = "NODE_VALUE_ENTRY_ID_EMPTY")
	private String entryId;

	@JsonProperty("CHAT_BOT_ID")
	@Size(min = 1, message = "NODE_VALUE_ENTRY_ID_EMPTY")
	private String chatBotId;

	@JsonProperty("URL")
	@Size(min = 1, message = "NODE_VALUE_URL_EMPTY")
	@URL(message = "NODE_VALUE_URL_INVALID")
	private String url;

	@JsonProperty("TYPE")
	@Pattern(regexp = "GET|POST|PUT|DELETE", message = "INVALID_SOURCE_TYPE")
	@Size(min = 1, message = "NODE_VALUE_TYPE_EMPTY")
	private String type;

	@JsonProperty("HEADERS")
	@Valid
	private List<Header> headers;

	@JsonProperty("FIELDS")
	@Valid
	private List<Field> fields;

	@JsonProperty("CODE")
	@Size(min = 1, message = "NODE_VALUE_CODE_EMPTY")
	private String code;

	@JsonProperty("MESSAGE")
	@Size(min = 1, message = "NODE_VALUE_MESSAGE_EMPTY")
	private String message;

	@JsonProperty("TEAMS")
	private List<String> teams;

	@JsonProperty("VARIABLE")
	@Size(min = 1, message = "NODE_VALUE_VARIABLE_EMPTY")
	private String variable;

	@JsonProperty("CONDITIONS")
	@Valid
	private List<Condition> conditions;

	@JsonProperty("ESCALATION")
	private String escalationId;

	public Values() {

	}

	public Values(@Size(min = 1, message = "NODE_VALUE_TO_EMPTY") String to,
			@Email(message = "INVALID_FROM_EMAILADDRESS") @Size(min = 1, message = "NODE_VALUE_FROM_EMPTY") String from,
			@Size(min = 1, message = "NODE_VALUE_SUBJECT_EMPTY") @Size(max = 255, message = "NODE_VALUE_SUBJECT_MAX") String subject,
			@Size(max = 2621400, message = "NODE_VALUE_BODY_MAX") String body,
			@Size(min = 1, message = "NODE_VALUE_MODULE_ID_EMPTY") String moduleId,
			@Size(min = 1, message = "NODE_VALUE_ENTRY_ID_EMPTY") String entryId,
			@Size(min = 1, message = "NODE_VALUE_ENTRY_ID_EMPTY") String chatBotId,
			@Size(min = 1, message = "NODE_VALUE_URL_EMPTY") @URL(message = "NODE_VALUE_URL_INVALID") String url,
			@Pattern(regexp = "GET|POST|PUT|DELETE", message = "INVALID_SOURCE_TYPE") @Size(min = 1, message = "NODE_VALUE_TYPE_EMPTY") String type,
			@Valid List<Header> headers, @Valid List<Field> fields,
			@Size(min = 1, message = "NODE_VALUE_CODE_EMPTY") String code,
			@Size(min = 1, message = "NODE_VALUE_MESSAGE_EMPTY") String message, List<String> teams,
			@Size(min = 1, message = "NODE_VALUE_VARIABLE_EMPTY") String variable, @Valid List<Condition> conditions,
			String escalationId) {
		super();
		this.to = to;
		this.from = from;
		this.subject = subject;
		this.body = body;
		this.moduleId = moduleId;
		this.entryId = entryId;
		this.chatBotId = chatBotId;
		this.url = url;
		this.type = type;
		this.headers = headers;
		this.fields = fields;
		this.code = code;
		this.message = message;
		this.teams = teams;
		this.variable = variable;
		this.conditions = conditions;
		this.escalationId = escalationId;
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

	public String getModuleId() {
		return moduleId;
	}

	public void setModuleId(String moduleId) {
		this.moduleId = moduleId;
	}

	public String getEntryId() {
		return entryId;
	}

	public void setEntryId(String entryId) {
		this.entryId = entryId;
	}

	public String getChatBotId() {
		return chatBotId;
	}

	public void setChatBotId(String chatBotId) {
		this.chatBotId = chatBotId;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public List<Header> getHeaders() {
		return headers;
	}

	public void setHeaders(List<Header> headers) {
		this.headers = headers;
	}

	public List<Field> getFields() {
		return fields;
	}

	public void setFields(List<Field> fields) {
		this.fields = fields;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public List<String> getTeams() {
		return teams;
	}

	public void setTeams(List<String> teams) {
		this.teams = teams;
	}

	public String getVariable() {
		return variable;
	}

	public void setVariable(String variable) {
		this.variable = variable;
	}

	public List<Condition> getConditions() {
		return conditions;
	}

	public void setConditions(List<Condition> conditions) {
		this.conditions = conditions;
	}

	public String getEscalationId() {
		return escalationId;
	}

	public void setEscalationId(String escalationId) {
		this.escalationId = escalationId;
	}

}
