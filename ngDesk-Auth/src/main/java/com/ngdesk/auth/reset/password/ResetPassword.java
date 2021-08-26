package com.ngdesk.auth.reset.password;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ResetPassword {
	@JsonProperty("PASSWORD")
	@NotNull(message = "PASSWORD_NOT_NULL")
	@Size(min = 1, message = "PASSWORD_NOT_EMPTY")
	@Pattern(regexp = "^(?=.*?[A-Z])(?=.*?[_#?!@$%^&*-]).{8,100}$", message = "PASSWORD_INVALID")
	private String password;

	@JsonProperty("COMPANY_SUBDOMAIN")
	@Field("COMPANY_SUBDOMAIN")
	private String subDomain;

	@JsonProperty("USER_UUID")
	@NotNull(message = "UUID_NOT_NULL")
	@Size(min = 1, message = "UUID_NOT_EMPTY")
	private String uuid;

	@JsonProperty("TEMP_UUID")
	@NotNull(message = "TEMP_UUID_NOT_NULL")
	@Size(min = 1, message = "TEMP_UUID_NOT_EMPTY")
	private String tempUuid;

	public ResetPassword(
			@NotNull(message = "PASSWORD_NOT_NULL") @Size(min = 1, message = "PASSWORD_NOT_EMPTY") @Pattern(regexp = "^(?=.*?[A-Z])(?=.*?[_#?!@$%^&*-]).{8,100}$", message = "PASSWORD_INVALID") String password,
			String subDomain,
			@NotNull(message = "UUID_NOT_NULL") @Size(min = 1, message = "UUID_NOT_EMPTY") String uuid,
			@NotNull(message = "TEMP_UUID_NOT_NULL") @Size(min = 1, message = "TEMP_UUID_NOT_EMPTY") String tempUuid) {
		super();
		this.password = password;
		this.subDomain = subDomain;
		this.uuid = uuid;
		this.tempUuid = tempUuid;
	}

	public ResetPassword() {

	}

	public String getPassword() {
		return password;
	}

	public String getSubDomain() {
		return subDomain;
	}

	public String getUuid() {
		return uuid;
	}

	public String getTempUuid() {
		return tempUuid;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setSubDomain(String subDomain) {
		this.subDomain = subDomain;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public void setTempUuid(String tempUuid) {
		this.tempUuid = tempUuid;
	}

}
