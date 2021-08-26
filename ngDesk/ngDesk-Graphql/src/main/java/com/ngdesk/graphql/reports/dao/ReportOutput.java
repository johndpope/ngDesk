package com.ngdesk.graphql.reports.dao;

public class ReportOutput {
	
	private String data;
	
	private String fileName;

	public ReportOutput(String data, String fileName) {
		super();
		this.data = data;
		this.fileName = fileName;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

}
