package com.ngdesk.workflow.dao;

import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MakePhoneCallNode extends Node {

	@JsonProperty("TO")
	@Field("TO")
	private String to;

	@JsonProperty("BODY")
	@Field("BODY")
	private String body;

	public MakePhoneCallNode() {

	}

	public MakePhoneCallNode(String to, String body) {
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
