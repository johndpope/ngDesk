package com.ngdesk.graphql.workflow;

import org.springframework.data.mongodb.core.mapping.Field;

public class Connection {

	@Field("TITLE")
	private String title;

	@Field("FROM")
	private String from;

	@Field("TO_NODE")
	private String toNode;

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
