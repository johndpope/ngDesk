package com.ngdesk.modules.importcsv;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CsvImportData {

	@JsonProperty("FILE")
	private String file;

	@JsonProperty("FILE_TYPE")
	private String fileType;

	@JsonProperty("FILE_NAME")
	private String fileName;

	@JsonProperty("HEADERS")
	private Map<String, String> headers = new HashMap<String, String>();

	CsvImportData() {
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
