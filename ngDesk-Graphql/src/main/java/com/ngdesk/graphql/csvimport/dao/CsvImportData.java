package com.ngdesk.graphql.csvimport.dao;

import java.util.List;

public class CsvImportData {

	private String file;

	private String fileType;

	private String fileName;

	private List<CsvHeaders> headers;

	public CsvImportData() {

	}

	public CsvImportData(String file, String fileType, String fileName, List<CsvHeaders> headers) {
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

	public List<CsvHeaders> getHeaders() {
		return headers;
	}

	public void setHeaders(List<CsvHeaders> headers) {
		this.headers = headers;
	}

}
