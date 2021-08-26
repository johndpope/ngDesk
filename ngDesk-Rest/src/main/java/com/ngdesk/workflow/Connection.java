package com.ngdesk.workflow;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Connection {

	@JsonProperty("TITLE")
	@NotEmpty(message = "TITLE_REQUIRED")
	private String title;

	@JsonProperty("FROM")
	@NotNull(message = "CONNECTION_FROM_NULL")
	private String from;

	@JsonProperty("TO_NODE")
	@NotNull(message = "CONNECTION_TO_NULL")
	private String toNode;

	public Connection() {

	}

	public Connection(@NotEmpty(message = "TITLE_REQUIRED") String title,
			@NotNull(message = "CONNECTION_FROM_NULL") String from,
			@NotNull(message = "CONNECTION_TO_NULL") String toNode) {
		super();
		this.title = title;
		this.from = from;
		this.toNode = toNode;
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

}
