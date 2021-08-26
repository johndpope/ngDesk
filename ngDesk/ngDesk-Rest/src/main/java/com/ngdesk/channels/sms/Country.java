package com.ngdesk.channels.sms;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Country {
	@JsonProperty("COUNTRY_NAME")
	private String countryName;

	@JsonProperty("COUNTRY_CODE")
	private String countryCode;

	@JsonProperty("COUNTRY_FLAG")
	private String countryFlag;

	public Country() {
	}

	public Country(String countryName, String countryCode, String countryFlag) {
		super();
		this.countryName = countryName;
		this.countryCode = countryCode;
		this.countryFlag = countryFlag;
	}

	public String getCountryName() {
		return countryName;
	}

	public void setCountryName(String countryName) {
		this.countryName = countryName;
	}

	public String getCountryCode() {
		return countryCode;
	}

	public void setCountryCode(String countryCode) {
		this.countryCode = countryCode;
	}

	public String getCountryFlag() {
		return countryFlag;
	}

	public void setCountryFlag(String countryFlag) {
		this.countryFlag = countryFlag;
	}

}
