package com.ngdesk.channels.facebook;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Change {
	@JsonProperty("field")
	private String field;

	@JsonProperty("value")
	private Value value;

	public Change() {
	}

	public Change(String field, Value value) {
		super();
		this.field = field;
		this.value = value;
	}

	public String getField() {
		return field;
	}

	public void setField(String field) {
		this.field = field;
	}

	public Value getValue() {
		return value;
	}

	public void setValue(Value value) {
		this.value = value;
	}

}
