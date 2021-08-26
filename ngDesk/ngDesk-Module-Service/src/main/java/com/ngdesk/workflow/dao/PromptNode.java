package com.ngdesk.workflow.dao;

import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PromptNode extends Node {

	@JsonProperty("VALUES")
	@Field("VALUES")
	private Values value;

	public PromptNode() {

	}

	public PromptNode(Values value) {
		super();
		this.value = value;
	}

	public Values getValue() {
		return value;
	}

	public void setValue(Values value) {
		this.value = value;
	}

}
