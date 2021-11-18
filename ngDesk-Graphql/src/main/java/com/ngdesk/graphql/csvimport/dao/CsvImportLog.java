package com.ngdesk.graphql.csvimport.dao;

public class CsvImportLog {

	private int lineNumber;

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
