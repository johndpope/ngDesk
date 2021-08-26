package com.ngdesk.users;

import java.sql.Timestamp;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Token {

	@JsonProperty("TYPE")
	@NotEmpty(message = "TYPE_REQUIRED")
	@Pattern(regexp = "ANDROID|IOS|WEB", message = "INVALID_TOKEN_TYPE")
	private String type;

	@JsonProperty("TOKEN")
	@NotEmpty(message = "TOKEN_REQUIRED")
	private String token;

	@JsonProperty("DATE_CREATED")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
	private Timestamp dateCreated;

	@JsonProperty("DEVICE_UUID")
	private String deviceUUID;

	public Token() {

	}

	public Token(
			@NotEmpty(message = "TYPE_REQUIRED") @Pattern(regexp = "ANDROID|IOS|WEB", message = "INVALID_TOKEN_TYPE") String type,
			@NotEmpty(message = "TOKEN_REQUIRED") String token, Timestamp dateCreated, String deviceUUID) {
		super();
		this.type = type;
		this.token = token;
		this.dateCreated = dateCreated;
		this.deviceUUID = deviceUUID;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public Timestamp getDateCreated() {
		return dateCreated;
	}

	public void setDateCreated(Timestamp dateCreated) {
		this.dateCreated = dateCreated;
	}

	public String getDeviceUUID() {
		return deviceUUID;
	}

	public void setDeviceUUID(String deviceUUID) {
		this.deviceUUID = deviceUUID;
	}

}
