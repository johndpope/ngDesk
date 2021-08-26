package com.ngdesk.graphql.role.dao;

public class EditablePermission {

	public String fieldId;

	public boolean notEditable;

	public EditablePermission() {

	}

	public EditablePermission(String fieldId, boolean notEditable) {
		super();
		this.fieldId = fieldId;
		this.notEditable = notEditable;
	}

	public String getFieldId() {
		return fieldId;
	}

	public void setFieldId(String fieldId) {
		this.fieldId = fieldId;
	}

	public boolean isNotEditable() {
		return notEditable;
	}

	public void setNotEditable(boolean notEditable) {
		this.notEditable = notEditable;
	}

}
