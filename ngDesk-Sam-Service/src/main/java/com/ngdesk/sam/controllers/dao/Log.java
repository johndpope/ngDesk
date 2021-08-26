package com.ngdesk.sam.controllers.dao;

import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Log {
	
	@Id
	private String id;

	@JsonProperty("LOGING_LEVEL")
	@Field("LOGGING_LEVEL")
	private String loggingLevel;

	@JsonProperty("LOG_MESSAGE")
	@Field("LOG_MESSAGE")
	private String logMessage;

	@JsonProperty("APPLICATION")
	@Field("APPLICATION")
	private String applicationName;

	@JsonProperty("DATE_CREATED")
	@Field("DATE_CREATED")
	private Date dateCreated;

	@JsonProperty("COMPANY_ID")
	@Field("COMPANY_ID")
	private String companyId;

	@JsonProperty("CONTROLLER_ID")
	@Field("CONTROLLER_ID")
	private String controllerId;

	public Log() {

	}

	
	public Log(String id, String loggingLevel, String logMessage, String applicationName, Date dateCreated,
			String companyId, String controllerId) {
		super();
		this.id = id;
		this.loggingLevel = loggingLevel;
		this.logMessage = logMessage;
		this.applicationName = applicationName;
		this.dateCreated = dateCreated;
		this.companyId = companyId;
		this.controllerId = controllerId;
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

	public String getApplicationName() {
		return applicationName;
	}

	public void setApplicationName(String applicationName) {
		this.applicationName = applicationName;
	}

	public Date getDateCreated() {
		return dateCreated;
	}

	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}

	public String getCompanyId() {
		return companyId;
	}

	public void setCompanyId(String companyId) {
		this.companyId = companyId;
	}


	public String getId() {
		return id;
	}


	public void setId(String id) {
		this.id = id;
	}


	public String getControllerId() {
		return controllerId;
	}


	public void setControllerId(String controllerId) {
		this.controllerId = controllerId;
	}


}