package com.ngdesk.report.dao;

import java.util.List;

import com.ngdesk.commons.annotations.CustomNotEmpty;

public class ReportField {

	@CustomNotEmpty(message = "DAO_VARIABLE_REQUIRED", values = { "FIELD_ID" })
	private String fieldId;

	private List<String> data;

	public ReportField() {

	}

	public ReportField(String fieldId, List<String> data) {
		super();
		this.fieldId = fieldId;
		this.data = data;
	}

	public String getFieldId() {
		return fieldId;
	}

	public void setFieldId(String fieldId) {
		this.fieldId = fieldId;
	}

	public List<String> getData() {
		return data;
	}

	public void setData(List<String> data) {
		this.data = data;
	}

}
