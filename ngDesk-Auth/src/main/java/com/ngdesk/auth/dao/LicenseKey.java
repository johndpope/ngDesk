package com.ngdesk.auth.dao;

import com.fasterxml.jackson.annotation.JsonProperty;

public class LicenseKey {

	@JsonProperty("LICENSE_KEY")
	private String licenseKey;

	public LicenseKey() {

	}

	public LicenseKey(String licenseKey) {
		super();
		this.licenseKey = licenseKey;
	}

	public String getLicenseKey() {
		return licenseKey;
	}

	public void setLicenseKey(String licenseKey) {
		this.licenseKey = licenseKey;
	}

}
