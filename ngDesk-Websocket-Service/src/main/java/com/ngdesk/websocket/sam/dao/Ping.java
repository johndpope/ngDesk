package com.ngdesk.websocket.sam.dao;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Ping {

	@JsonProperty("CONTROLLER_ID")
	private String controllerId;

	@JsonProperty("APPLICATION_NAME")
	private String applicationName;

	public Ping() {

	}

	public Ping(String controllerId, String applicationName) {
		super();
		this.controllerId = controllerId;
		this.applicationName = applicationName;
	}

	public String getControllerId() {
		return controllerId;
	}

	public void setControllerId(String controllerId) {
		this.controllerId = controllerId;
	}

	public String getApplicationName() {
		return applicationName;
	}

	public void setApplicationName(String applicationName) {
		this.applicationName = applicationName;
	}

}
