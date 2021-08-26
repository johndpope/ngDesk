package com.ngdesk.users;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ResetPassword {
	@JsonProperty("PASSWORD")
	@NotNull(message = "PASSWORD_NOT_NULL")
	@Size(min = 1, message = "PASSWORD_NOT_EMPTY")
	@Pattern(regexp = "^(?=.*?[A-Z])(?=.*?[_#?!@$%^&*-]).{8,100}$", message = "PASSWORD_INVALID")
	private String password;

	@JsonProperty("UUID")
	@NotNull(message = "UUID_NOT_NULL")
	@Size(min = 1, message = "UUID_NOT_EMPTY")
	private String uuid;

	@JsonProperty("TEMP_UUID")
	@NotNull(message = "TEMP_UUID_NOT_NULL")
	@Size(min = 1, message = "TEMP_UUID_NOT_EMPTY")
	private String tempUuid;

	public ResetPassword(
			@NotNull(message = "PASSWORD_NOT_NULL") @Size(min = 1, message = "PASSWORD_NOT_EMPTY") String password,
			@NotNull(message = "UUID_NOT_NULL") @Size(min = 1, message = "UUID_NOT_EMPTY") String uuid,
			@NotNull(message = "TEMP_UUID_NOT_NULL") @Size(min = 1, message = "TEMP_UUID_NOT_EMPTY") String tempUuid) {
		super();
		this.password = password;
		this.uuid = uuid;
		this.tempUuid = tempUuid;
	}

	public String getTempUuid() {
		return tempUuid;
	}

	public void setTempUuid(String tempUuid) {
		this.tempUuid = tempUuid;
	}

	public ResetPassword() {
		super();
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

}
