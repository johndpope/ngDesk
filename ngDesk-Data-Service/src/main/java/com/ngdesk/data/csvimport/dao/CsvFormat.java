package com.ngdesk.data.csvimport.dao;

import io.swagger.v3.oas.annotations.media.Schema;

public class CsvFormat {

	@Schema(required = false, description = "csv separator", example = "-")
	private String separator;

	@Schema(required = false, description = "Date format of csv file", example = "dd/MM/yyyy")
	private String dateFormat;

	@Schema(required = false, description = "Time format of csv file", example = "h:mm:ss")
	private String timeFormat;

	@Schema(required = false, description = "Date and time format for csv file", example = "dd/MM/yyyy hh:mm:ss")
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
