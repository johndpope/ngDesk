package com.ngdesk.modules.list.layouts;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ngdesk.modules.fields.Field;

public class Column {

	@JsonProperty("FIELDS")
	@Valid
	private List<String> fields;

	public Column() {

	}

	public Column(@Valid List<String> fields) {
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
