package com.ngdesk.data.csvimport.dao;

import io.swagger.v3.oas.annotations.media.Schema;

public class CsvImportLog {

	@Schema(required = false, description = "Shows the line number of error in csv file")
	private int lineNumber;

	@Schema(required = false, description = "Shows the error message in csv file")
	private String errorMessage;

	public CsvImportLog() {

	}

	public CsvImportLog(int lineNumber, String errorMessage) {
		super();
		this.lineNumber = lineNumber;
		this.errorMessage = errorMessage;
	}

	public int getLineNumber() {
		return lineNumber;
	}

	public void setLineNumber(int lineNumber) {
		this.lineNumber = lineNumber;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

}
