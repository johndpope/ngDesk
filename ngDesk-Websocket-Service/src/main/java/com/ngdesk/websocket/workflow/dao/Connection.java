package com.ngdesk.websocket.workflow.dao;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Connection {

	@JsonProperty("TITLE")
	private String title;

	@JsonProperty("FROM")
	private String from;

	@JsonProperty("TO_NODE")
	private String toNode;

	@JsonProperty("ON_ERROR")
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
