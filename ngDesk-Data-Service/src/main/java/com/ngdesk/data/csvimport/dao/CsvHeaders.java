package com.ngdesk.data.csvimport.dao;

public class CsvHeaders {

	private String fieldId;

	private String headerName;

	public CsvHeaders() {

	}

	public CsvHeaders(String fieldId, String headerName) {
		super();
		this.fieldId = fieldId;
		this.headerName = headerName;
	}

	public String getFieldId() {
		return fieldId;
	}

	public void setFieldId(String fieldId) {
		this.fieldId = fieldId;
	}

	public String getHeaderName() {
		return headerName;
	}

	public void setHeaderName(String headerName) {
		this.headerName = headerName;
	}

}
