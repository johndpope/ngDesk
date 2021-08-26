package com.ngdesk.workflow.dao;

import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonProperty;

@Component
public class SendSmsNode extends Node {
	@JsonProperty("TO")
	@Field("TO")
	private String to;

	@JsonProperty("BODY")
	@Field("BODY")
	private String body;

	public SendSmsNode() {
	}

	public SendSmsNode(String to, String body) {
		super();
		this.to = to;
		this.body = body;
	}

	public String getTo() {
		return to;
	}

	public void setTo(String to) {
		this.to = to;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

}
