package com.ngdesk.data.csvimport.dao;

import java.util.List;

import com.ngdesk.commons.annotations.CustomNotEmpty;
import com.ngdesk.commons.annotations.CustomNotNull;

import io.swagger.v3.oas.annotations.media.Schema;

public class CsvImportData {
	
	@Schema(required = true, description = "Csv file",example = "file")
	@CustomNotEmpty(message = "DAO_VARIABLE_REQUIRED", values = { "FILE" })
	private String file;
	
	@Schema(required = true,description = "Type of csv file",example = "csv-file-type")
	@CustomNotEmpty(message = "DAO_VARIABLE_REQUIRED", values = { "FILE_TYPE" })
	private String fileType;
	
	@Schema(required = true,description = "Name of the file",example = "file-name")
	@CustomNotEmpty(message = "DAO_VARIABLE_REQUIRED", values = { "FILE_NAME" })
	private String fileName;
	
	@Schema(required = true,description = "List of csv headers",example = "csv-headers")
	@CustomNotNull(message = "NOT_NULL", values = { "CSV_HEADERS" })
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
