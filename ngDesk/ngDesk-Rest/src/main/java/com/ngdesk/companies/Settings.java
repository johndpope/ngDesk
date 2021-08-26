package com.ngdesk.companies;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Settings {

	@JsonProperty("ENABLE_SIGNUPS")
	@NotNull(message = "ENABLE_SIGNUPS_NOT_NULL")
	private boolean enableSignups;

	@JsonProperty("ENABLE_GOOGLE_SIGNUPS")
	@NotNull(message = "ENABLE_GOOGLE_SIGNUPS_NOT_NULL")
	private boolean enableGoogleSignups;

	@JsonProperty("MAX_LOGIN_RETRIES")
	@NotNull(message = "MAX_LOGIN_RETRIES_NOT_NULL")
	private int maxLoginRetries;

	public Settings() {

	}

	public Settings(@NotNull(message = "ENABLE_SIGNUPS_NOT_NULL") boolean enableSignups,
			@NotNull(message = "ENABLE_GOOGLE_SIGNUPS_NOT_NULL") boolean enableGoogleSignups,
			@NotNull(message = "MAX_LOGIN_RETRIES_NOT_NULL") int maxLoginRetries) {
		super();
		this.enableSignups = enableSignups;
		this.enableGoogleSignups = enableGoogleSignups;
		this.maxLoginRetries = maxLoginRetries;
	}

	public boolean isEnableSignups() {
		return enableSignups;
	}

	public void setEnableSignups(boolean enableSignups) {
		this.enableSignups = enableSignups;
	}

	public boolean isEnableGoogleSignups() {
		return enableGoogleSignups;
	}

	public void setEnableGoogleSignups(boolean enableGoogleSignups) {
		this.enableGoogleSignups = enableGoogleSignups;
	}

	public int getMaxLoginRetries() {
		return maxLoginRetries;
	}

	public void setMaxLoginRetries(int maxLoginRetries) {
		this.maxLoginRetries = maxLoginRetries;
	}

}
