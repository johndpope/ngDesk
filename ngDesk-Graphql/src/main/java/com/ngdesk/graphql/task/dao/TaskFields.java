package com.ngdesk.graphql.task.dao;

public class TaskFields {

	private String fieldId;

	private String value;

	public TaskFields() {

	}

	public TaskFields(String fieldId, String value) {
		super();
		this.fieldId = fieldId;
		this.value = value;
	}

	public String getFieldId() {
		return fieldId;
	}

	public void setFieldId(String fieldId) {
		this.fieldId = fieldId;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

}
