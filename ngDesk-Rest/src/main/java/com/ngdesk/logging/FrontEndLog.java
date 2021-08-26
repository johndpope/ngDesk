package com.ngdesk.logging;

import java.util.List;

public class FrontEndLog {

	private List<String> additional;
	private String fileName;
	private Integer level;
	private String lineNumber;
	private String message;
	private String timestamp;

	public FrontEndLog() {

	}

	public FrontEndLog(List<String> additional, String fileName, Integer level, String lineNumber, String message,
			String timestamp) {
		super();
		this.additional = additional;
		this.fileName = fileName;
		this.level = level;
		this.lineNumber = lineNumber;
		this.message = message;
		this.timestamp = timestamp;
	}

	public List<String> getAdditional() {
		return additional;
	}

	public void setAdditional(List<String> additional) {
		this.additional = additional;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public Integer getLevel() {
		return level;
	}

	public void setLevel(Integer level) {
		this.level = level;
	}

	public String getLineNumber() {
		return lineNumber;
	}

	public void setLineNumber(String lineNumber) {
		this.lineNumber = lineNumber;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

}
