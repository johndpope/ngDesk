package com.ngdesk.companies;

import javax.validation.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ngdesk.annotations.Base64Validator;

@JsonInclude(Include.NON_NULL)
public class LogoTemplate {

	@JsonProperty("FILE")
	@NotEmpty(message = "FILE_REQUIRED")
	@Base64Validator
	String file;

	@JsonProperty("FILENAME")
	@NotEmpty(message = "FILE_NAME_REQUIRED")
	String filename;

	@JsonProperty("HEADER")
	String header;

	public LogoTemplate() {

	}

	public LogoTemplate(@NotEmpty(message = "FILE_REQUIRED") String file,
			@NotEmpty(message = "FILENAME_REQUIRED") String filename, String header) {
		super();
		this.file = file;
		this.filename = filename;
		this.header = header;
	}

	public String getFile() {
		return file;
	}

	public void setFile(String file) {
		this.file = file;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public String getHeader() {
		return header;
	}

	public void setHeader(String header) {
		this.header = header;
	}

}
