package com.ngdesk.auth.forgot.password;

import java.util.Date;

import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonProperty;

@Component
public class InviteTracking {

	@JsonProperty("USER_UUID")
	@Field("USER_UUID")
	private String userUUID;

	@JsonProperty("DATE_CREATED")
	@Field("DATE_CREATED")
	private Date date = new Date();

	@JsonProperty("TYPE")
	@Field("TYPE")
	private String type;

	@JsonProperty("TEMP_UUID")
	@Field("TEMP_UUID")
	private String tempUUID;

	public InviteTracking() {

	}

	public InviteTracking(String userUUID, Date date, String type, String tempUUID) {
		super();
		this.userUUID = userUUID;
		this.date = date;
		this.type = type;
		this.tempUUID = tempUUID;
	}

	public String getUserUUID() {
		return userUUID;
	}

	public void setUserUUID(String userUUID) {
		this.userUUID = userUUID;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getTempUUID() {
		return tempUUID;
	}

	public void setTempUUID(String tempUUID) {
		this.tempUUID = tempUUID;
	}

}
