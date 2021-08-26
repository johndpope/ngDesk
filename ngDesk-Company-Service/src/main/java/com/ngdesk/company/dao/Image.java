package com.ngdesk.company.dao;

import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Image {

	@JsonProperty("FILENAME")
	@Field("FILENAME")
	private String fileName;

	@JsonProperty("FILE")
	@Field("FILE")
	private String file;

	@JsonProperty("HEADER")
	@Field("HEADER")
	private String header;

	public Image(String fileName, String file, String header) {
		super();
		this.fileName = fileName;
		this.file = file;
		this.header = header;
	}

	public Image() {
		super();
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getFile() {
		return file;
	}

	public void setFile(String file) {
		this.file = file;
	}

	public String getHeader() {
		return header;
	}

	public void setHeader(String header) {
		this.header = header;
	}

}
