package com.ngdesk.workflow.notify.dao;

import java.util.Date;

import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Token {

	@JsonProperty("DATE_CREATED")
	@Field("DATE_CREATED")
	private Date dateCreated;

	@JsonProperty("TOKEN")
	@Field("TOKEN")
	private String token;

	@JsonProperty("DEVICE_UUID")
	@Field("DEVICE_UUID")
	private String deviceUuid;

	public Token() {

	}

	public Token(Date dateCreated, String token, String deviceUuid) {
		super();
		this.dateCreated = dateCreated;
		this.token = token;
		this.deviceUuid = deviceUuid;
	}

	public Date getDateCreated() {
		return dateCreated;
	}

	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getDeviceUuid() {
		return deviceUuid;
	}

	public void setDeviceUuid(String deviceUuid) {
		this.deviceUuid = deviceUuid;
	}

}
