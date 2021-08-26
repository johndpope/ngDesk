package com.ngdesk.auth.forgot.password;

import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ForgotPassword {

	@JsonProperty("EMAIL_ADDRESS")
	@Field("EMAIL_ADDRESS")
	private String emailAddress;

	@JsonProperty("COMPANY_SUBDOMAIN")
	@Field("COMPANY_SUBDOMAIN")
	private String subDomain;

	public ForgotPassword() {

	}

	public ForgotPassword(String emailAddress, String subDomain) {
		super();
		this.emailAddress = emailAddress;
		this.subDomain = subDomain;
	}

	public String getEmailAddress() {
		return emailAddress;
	}

	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}

	public String getSubDomain() {
		return subDomain;
	}

	public void setSubDomain(String subDomain) {
		this.subDomain = subDomain;
	}

}
