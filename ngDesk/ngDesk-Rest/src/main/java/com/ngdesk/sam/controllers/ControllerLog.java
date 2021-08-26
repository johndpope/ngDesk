package com.ngdesk.sam.controllers;

import java.util.Date;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

public class ControllerLog {
	@JsonProperty("LOGING_LEVEL")
	private String loggingLevel;

	@JsonProperty("LOG_MESSAGE")
	private String logMessage;

	@JsonProperty("APPLICATION")
	private String application;

	@JsonProperty("DATE_CREATED")
	@JsonDeserialize(using = MongoDateConverter.class)
	private String dateCreated;

	public ControllerLog() {
	}

	public String getLoggingLevel() {
		return loggingLevel;
	}

	public void setLoggingLevel(String loggingLevel) {
		this.loggingLevel = loggingLevel;
	}

	public String getLogMessage() {
		return logMessage;
	}

	public void setLogMessage(String logMessage) {
		this.logMessage = logMessage;
	}

	public String getApplication() {
		return application;
	}

	public void setApplication(String application) {
		this.application = application;
	}

	public ControllerLog(String loggingLevel, String logMessage, String application, String dateCreated) {
		super();
		this.loggingLevel = loggingLevel;
		this.logMessage = logMessage;
		this.application = application;
		this.dateCreated = dateCreated;
	}

	public String getDateCreated() {
		return dateCreated;
	}

	public void setDateCreated(String dateCreated) {
		this.dateCreated = dateCreated;
	}

	
}
