package com.ngdesk.sam.controllers;

import javax.validation.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Instruction {
	@JsonProperty("APPLICATION_NAME")
	private String applicationName;

	@JsonProperty("ACTION")
	private String action;

	@JsonProperty("LOG_LEVEL")
	private String logLevel;

	public Instruction() {
		super();
	}

	public Instruction(String applicationName, String action, String logLevel) {
		super();
		this.applicationName = applicationName;
		this.action = action;
		this.logLevel = logLevel;
	}

	public String getApplicationName() {
		return applicationName;
	}

	public void setApplicationName(String applicationName) {
		this.applicationName = applicationName;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public String getLogLevel() {
		return logLevel;
	}

	public void setLogLevel(String logLevel) {
		this.logLevel = logLevel;
	}

}
