package com.ngdesk.websocket.sam.dao;

import java.util.Map;

import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Instruction {

	@JsonProperty("APPLICATION_NAME")
	@Field("APPLICATION_NAME")
	private String applicationName;

	@JsonProperty("ACTION")
	@Field("ACTION")
	private String action;

	@JsonProperty("LOG_LEVEL")
	@Field("LOG_LEVEL")
	private String logLevel;

	@JsonProperty("PROBE_INFO")
	@Field("PROBE_INFO")
	private Map<String, Object> probeInfo;

	public Instruction() {
		super();
	}

	public Instruction(String applicationName, String action, String logLevel, Map<String, Object> probeInfo) {
		super();
		this.applicationName = applicationName;
		this.action = action;
		this.logLevel = logLevel;
		this.probeInfo = probeInfo;
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

	public Map<String, Object> getProbeInfo() {
		return probeInfo;
	}

	public void setProbeInfo(Map<String, Object> probeInfo) {
		this.probeInfo = probeInfo;
	}

}
