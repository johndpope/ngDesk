package com.ngdesk.workflow.dao;

import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;

public class NotifyProbeNode extends Node {

	@JsonProperty("ACTION")
	@Field("ACTION")
	private String action;

	@JsonProperty("APPLICATION_NAME")
	@Field("APPLICATION_NAME")
	private String applicationName;

	@JsonProperty("LOG_LEVEL")
	@Field("LOG_LEVEL")
	private String logLevel;

	@JsonProperty("CONTROLLER")
	@Field("CONTROLLER")
	private String controller;

	public NotifyProbeNode() {

	}

	public NotifyProbeNode(String action, String applicationName, String logLevel, String controller) {
		super();
		this.action = action;
		this.applicationName = applicationName;
		this.logLevel = logLevel;
		this.controller = controller;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public String getApplicationName() {
		return applicationName;
	}

	public void setApplicationName(String applicationName) {
		this.applicationName = applicationName;
	}

	public String getLogLevel() {
		return logLevel;
	}

	public void setLogLevel(String logLevel) {
		this.logLevel = logLevel;
	}

	public String getController() {
		return controller;
	}

	public void setController(String controller) {
		this.controller = controller;
	}

}
