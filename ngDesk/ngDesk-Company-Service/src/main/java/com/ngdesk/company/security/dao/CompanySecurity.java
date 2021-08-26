package com.ngdesk.company.security.dao;

import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CompanySecurity {

	@JsonProperty("ENABLE_SIGNUPS")
	@Field("ENABLE_SIGNUPS")
	private boolean enableSignUps = true;

	@JsonProperty("MAX_LOGIN_RETRIES")
	@Field("MAX_LOGIN_RETRIES")
	private int maxLoginRetries;

	@JsonProperty("COMPANY_ID")
	@Field("COMPANY_ID")
	private String companyId;

	public CompanySecurity() {
	}

	public CompanySecurity(boolean enableSignUps, int maxLoginRetries, String companyId) {
		this.enableSignUps = enableSignUps;
		this.maxLoginRetries = maxLoginRetries;
		this.companyId = companyId;
	}

	public boolean isEnableSignUps() {
		return enableSignUps;
	}

	public void setEnableSignUps(boolean enableSignUps) {
		this.enableSignUps = enableSignUps;
	}

	public int getMaxLoginRetries() {
		return maxLoginRetries;
	}

	public void setMaxLoginRetries(int maxLoginRetries) {
		this.maxLoginRetries = maxLoginRetries;
	}

	public String getCompanyId() {
		return companyId;
	}

	public void setCompanyId(String companyId) {
		this.companyId = companyId;
	}

}
