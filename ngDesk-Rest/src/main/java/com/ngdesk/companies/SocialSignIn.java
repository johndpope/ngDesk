package com.ngdesk.companies;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SocialSignIn {

	@JsonProperty("ENABLE_FACEBOOK")
	private boolean enableFacebook;

	@JsonProperty("ENABLE_GOOGLE")
	private boolean enableGoogle;

	@JsonProperty("ENABLE_TWITTER")
	private boolean enableTwitter;

	@JsonProperty("ENABLE_MICROSOFT")
	private boolean enableMicrosoft;

	public SocialSignIn() {
	}

	public SocialSignIn(boolean enableFacebook, boolean enableGoogle, boolean enableTwitter, boolean enableMicrosoft) {
		super();
		this.enableFacebook = enableFacebook;
		this.enableGoogle = enableGoogle;
		this.enableTwitter = enableTwitter;
		this.enableMicrosoft = enableMicrosoft;
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
