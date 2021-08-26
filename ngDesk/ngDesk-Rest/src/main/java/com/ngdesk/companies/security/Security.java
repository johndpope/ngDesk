package com.ngdesk.companies.security;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties("_class")
public class Security {

	@JsonProperty("ENABLE_SIGNUPS")
	@NotNull(message = "ENABLE_SIGNUPS_NOT_NULL")
	private boolean enableSignups;

	@JsonProperty("MAX_LOGIN_RETRIES")
	@NotNull(message = "MAX_LOGIN_RETRIES_NOT_NULL")
	private int maxLoginRetries;

	@JsonProperty("COMPANY_ID")
	private String companyId;

	public Security() {

	}

	public Security(@NotNull(message = "ENABLE_SIGNUPS_NOT_NULL") boolean enableSignups,
			@NotNull(message = "MAX_LOGIN_RETRIES_NOT_NULL") int maxLoginRetries, String companyId) {
		super();
		this.enableSignups = enableSignups;
		this.maxLoginRetries = maxLoginRetries;
		this.companyId = companyId;
	}

	public boolean isEnableSignups() {
		return enableSignups;
	}

	public void setEnableSignups(boolean enableSignups) {
		this.enableSignups = enableSignups;
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
