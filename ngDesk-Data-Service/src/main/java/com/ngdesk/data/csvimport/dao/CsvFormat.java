package com.ngdesk.data.csvimport.dao;

public class CsvFormat {

	private String separator;

	private String dateFormat;

	private String timeFormat;

	private String dateTimeFormat;

	public CsvFormat() {

	}

	public CsvFormat(String separator, String dateFormat, String timeFormat, String dateTimeFormat) {
		super();
		this.separator = separator;
		this.dateFormat = dateFormat;
		this.timeFormat = timeFormat;
		this.dateTimeFormat = dateTimeFormat;
	}

	public String getSeparator() {
		return separator;
	}

	public void setSeparator(String separator) {
		this.separator = separator;
	}

	public String getDateFormat() {
		return dateFormat;
	}

	public void setDateFormat(String dateFormat) {
		this.dateFormat = dateFormat;
	}

	public String getTimeFormat() {
		return timeFormat;
	}

	public void setTimeFormat(String timeFormat) {
		this.timeFormat = timeFormat;
	}

	public String getDateTimeFormat() {
		return dateTimeFormat;
	}

	public void setDateTimeFormat(String dateTimeFormat) {
		this.dateTimeFormat = dateTimeFormat;
	}

}
