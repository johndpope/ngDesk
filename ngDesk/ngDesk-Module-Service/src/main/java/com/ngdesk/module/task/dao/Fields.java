package com.ngdesk.module.task.dao;

public class Fields {

	private String fieldId;
	private String value;

	public Fields() {
		super();
	}

	public Fields(String fieldId, String value) {
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
