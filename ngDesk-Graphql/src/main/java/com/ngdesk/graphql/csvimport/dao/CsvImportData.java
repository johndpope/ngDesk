package com.ngdesk.graphql.csvimport.dao;

import java.util.Map;

public class CsvImportData {

	private String file;

	private String fileType;

	private String fileName;

	private Map<String, String> headers;

	public CsvImportData() {

	}

	public CsvImportData(String file, String fileType, String fileName, Map<String, String> headers) {
		super();
		this.file = file;
		this.fileType = fileType;
		this.fileName = fileName;
		this.headers = headers;
	}

	public String getFile() {
		return file;
	}

	public void setFile(String file) {
		this.file = file;
	}

	public String getFileType() {
		return fileType;
	}

	public void setFileType(String fileType) {
		this.fileType = fileType;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public Map<String, String> getHeaders() {
		return headers;
	}

	public void setHeaders(Map<String, String> headers) {
		this.headers = headers;
	}

}
