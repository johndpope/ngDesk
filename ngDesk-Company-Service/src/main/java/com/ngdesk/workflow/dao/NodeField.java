package com.ngdesk.workflow.dao;

import java.util.List;

import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ngdesk.commons.annotations.CustomNotEmpty;

import io.swagger.v3.oas.annotations.media.Schema;

public class NodeField {

	@Schema(required = true, description = "ID of the field to be added to the entry", example = "2d9b90c2-de8c-408c-bc75-4110a612c47c")
	@JsonProperty("FIELD")
	@Field("FIELD")
	@CustomNotEmpty(message = "DAO_VARIABLE_REQUIRED", values = { "FIELD" })
	private String field;

	@Schema(required = true, description = "value to be added to the field", example = "['Low']")
	@JsonProperty("VALUE")
	@Field("VALUE")
	@CustomNotEmpty(message = "DAO_VARIABLE_REQUIRED", values = { "VALUE" })
	private List<String> value;

	public NodeField(String field, List<String> value) {
		this.field = field;
		this.value = value;
	}

	public NodeField() {
	}

	public String getField() {
		return field;
	}

	public void setField(String field) {
		this.field = field;
	}

	public List<String> getValue() {
		return value;
	}

	public void setValue(List<String> value) {
		this.value = value;
	}

}
