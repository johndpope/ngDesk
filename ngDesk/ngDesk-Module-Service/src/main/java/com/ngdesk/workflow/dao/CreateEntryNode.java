package com.ngdesk.workflow.dao;

import java.util.List;

import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CreateEntryNode extends Node {

	@JsonProperty("MODULE")
	@Field("MODULE")
	private String module;

	@JsonProperty("FIELDS")
	@Field("FIELDS")
	private List<NodeField> fields;

	public CreateEntryNode() {
	}

	public String getModule() {
		return module;
	}

	public void setModule(String module) {
		this.module = module;
	}

	public List<NodeField> getFields() {
		return fields;
	}

	public void setFields(List<NodeField> fields) {
		this.fields = fields;
	}

	public CreateEntryNode(String module, List<NodeField> fields) {
		this.module = module;
		this.fields = fields;
	}

}
