package com.ngdesk.escalation.notify;

import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Token {

	@Field("TOKEN")
	private String token;

	@Field("DEVICE_UUID")
	private String deviceUuid;

	public Token() {

	}

	public Token(String token, String deviceUuid) {
		this.token = token;
		this.deviceUuid = deviceUuid;
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
