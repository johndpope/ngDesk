package com.ngdesk.auth.company.dao;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Company {

	@Id
	@JsonProperty("COMPANY_ID")
	private String companyId;

	@JsonProperty("COMPANY_SUBDOMAIN")
	@Field("COMPANY_SUBDOMAIN")
	private String companySubdomain;

	@JsonProperty("COMPANY_UUID")
	@Field("COMPANY_UUID")
	private String companyUuid;

	@JsonProperty("TIMEZONE")
	@Field("TIMEZONE")
	private String timezone;

	@JsonProperty("FORGOT_PASSWORD_MESSAGE")
	@Field("FORGOT_PASSWORD_MESSAGE")
	private ForgotPasswordMessage forgotPasswordMessage;

	public Company() {

	}

	public Company(String companyId, String companySubdomain, String companyUuid, String timezone,
			ForgotPasswordMessage forgotPasswordMessage) {
		super();
		this.companyId = companyId;
		this.companySubdomain = companySubdomain;
		this.companyUuid = companyUuid;
		this.timezone = timezone;
		this.forgotPasswordMessage = forgotPasswordMessage;
	}

	public String getCompanyId() {
		return companyId;
	}

	public void setCompanyId(String companyId) {
		this.companyId = companyId;
	}

	public String getCompanySubdomain() {
		return companySubdomain;
	}

	public void setCompanySubdomain(String companySubdomain) {
		this.companySubdomain = companySubdomain;
	}

	public String getCompanyUuid() {
		return companyUuid;
	}

	public void setCompanyUuid(String companyUuid) {
		this.companyUuid = companyUuid;
	}

	public String getTimezone() {
		return timezone;
	}

	public void setTimezone(String timezone) {
		this.timezone = timezone;
	}

	public ForgotPasswordMessage getForgotPasswordMessage() {
		return forgotPasswordMessage;
	}

	public void setForgotPasswordMessage(ForgotPasswordMessage forgotPasswordMessage) {
		this.forgotPasswordMessage = forgotPasswordMessage;
	}

}
