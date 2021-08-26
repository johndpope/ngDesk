package com.ngdesk.ldap;

import java.util.List;

import javax.validation.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Ad {

	@JsonProperty("SSO_URL")
	@NotEmpty(message = "SSO_URL_REQUIRED")
	private String ssoUrl;

	@JsonProperty("FINGERPRINT")
	@NotEmpty(message = "FINGERPRINT_REQUIRED")
	private String certificateFingerprint;

	@JsonProperty("LOGOUT_URL")
	private String remoteLogoutUrl;

	@JsonProperty("IP_RANGES")
	private List<String> ipRanges;

	public Ad() {

	}

	public Ad(@NotEmpty(message = "SSO_URL_REQUIRED") String ssoUrl,
			@NotEmpty(message = "FINGERPRINT_REQUIRED") String certificateFingerprint, String remoteLogoutUrl,
			List<String> ipRanges) {
		super();
		this.ssoUrl = ssoUrl;
		this.certificateFingerprint = certificateFingerprint;
		this.remoteLogoutUrl = remoteLogoutUrl;
		this.ipRanges = ipRanges;
	}

	public String getSsoUrl() {
		return ssoUrl;
	}

	public void setSsoUrl(String ssoUrl) {
		this.ssoUrl = ssoUrl;
	}

	public String getCertificateFingerprint() {
		return certificateFingerprint;
	}

	public void setCertificateFingerprint(String certificateFingerprint) {
		this.certificateFingerprint = certificateFingerprint;
	}

	public String getRemoteLogoutUrl() {
		return remoteLogoutUrl;
	}

	public void setRemoteLogoutUrl(String remoteLogoutUrl) {
		this.remoteLogoutUrl = remoteLogoutUrl;
	}

	public List<String> getIpRanges() {
		return ipRanges;
	}

	public void setIpRanges(List<String> ipRanges) {
		this.ipRanges = ipRanges;
	}

}
