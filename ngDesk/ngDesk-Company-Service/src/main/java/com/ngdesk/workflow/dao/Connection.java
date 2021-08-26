package com.ngdesk.workflow.dao;

import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ngdesk.commons.annotations.CustomNotEmpty;

import io.swagger.v3.oas.annotations.media.Schema;

public class Connection {

	@Schema(required = true, description = "title for the connection")
	@JsonProperty("TITLE")
	@Field("TITLE")
	@CustomNotEmpty(message = "DAO_VARIABLE_REQUIRED", values = { "CONNECTION_TITLE" })
	private String title;

	@Schema(required = true, description = "the node id from where the connection is posted")
	@JsonProperty("FROM")
	@Field("FROM")
	@CustomNotEmpty(message = "DAO_VARIABLE_REQUIRED", values = { "CONNECTION_FROM" })
	private String from;

	@Schema(required = true, description = "the node id to where the connection is posted")
	@JsonProperty("TO_NODE")
	@Field("TO_NODE")
	@CustomNotEmpty(message = "DAO_VARIABLE_REQUIRED", values = { "CONNECTION_TO" })
	private String toNode;

	@JsonProperty("ON_ERROR")
	@Field("ON_ERROR")
	private boolean onError;

	public Connection() {

	}

	public Connection(String title, String from, String toNode, boolean onError) {
		super();
		this.title = title;
		this.from = from;
		this.toNode = toNode;
		this.onError = onError;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public String getToNode() {
		return toNode;
	}

	public void setToNode(String toNode) {
		this.toNode = toNode;
	}

	public boolean isOnError() {
		return onError;
	}

	public void setOnError(boolean onError) {
		this.onError = onError;
	}

}
