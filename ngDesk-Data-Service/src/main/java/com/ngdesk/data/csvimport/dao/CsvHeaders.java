package com.ngdesk.data.csvimport.dao;

import io.swagger.v3.oas.annotations.media.Schema;

public class CsvHeaders {

	@Schema(required = false, description = "Field id")
	private String fieldId;

	@Schema(required = false, description = "Name of the header")
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
