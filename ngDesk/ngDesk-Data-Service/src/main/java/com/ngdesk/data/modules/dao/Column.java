package com.ngdesk.data.modules.dao;

import java.util.List;

import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Column {
	@JsonProperty("FIELDS")
	@Field("FIELDS")
	private List<String> fields;

	public Column() {

	}

	public Column(List<String> fields) {
		super();
		this.fields = fields;
	}

	public List<String> getFields() {
		return fields;
	}

	public void setFields(List<String> fields) {
		this.fields = fields;
	}
}
