package com.ngdesk.company.dao;

import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SocialSignIn {
	@JsonProperty("ENABLE_FACEBOOK")
	@Field("ENABLE_FACEBOOK")
	private boolean enableFacebook = true;

	@JsonProperty("ENABLE_GOOGLE")
	@Field("ENABLE_GOOGLE")
	private boolean enableGoogle = true;

	@JsonProperty("ENABLE_TWITTER")
	@Field("ENABLE_TWITTER")
	private boolean enableTwitter = true;

	@JsonProperty("ENABLE_MICROSOFT")
	@Field("ENABLE_MICROSOFT")
	private boolean enableMicrosoft = true;

	public SocialSignIn(boolean enableFacebook, boolean enableGoogle, boolean enableTwitter, boolean enableMicrosoft) {
		super();
		this.enableFacebook = enableFacebook;
		this.enableGoogle = enableGoogle;
		this.enableTwitter = enableTwitter;
		this.enableMicrosoft = enableMicrosoft;
	}

	public SocialSignIn() {
		super();
	}

	public boolean isEnableFacebook() {
		return enableFacebook;
	}

	public void setEnableFacebook(boolean enableFacebook) {
		this.enableFacebook = enableFacebook;
	}

	public boolean isEnableGoogle() {
		return enableGoogle;
	}

	public void setEnableGoogle(boolean enableGoogle) {
		this.enableGoogle = enableGoogle;
	}

	public boolean isEnableTwitter() {
		return enableTwitter;
	}

	public void setEnableTwitter(boolean enableTwitter) {
		this.enableTwitter = enableTwitter;
	}

	public boolean isEnableMicrosoft() {
		return enableMicrosoft;
	}

	public void setEnableMicrosoft(boolean enableMicrosoft) {
		this.enableMicrosoft = enableMicrosoft;
	}

}
