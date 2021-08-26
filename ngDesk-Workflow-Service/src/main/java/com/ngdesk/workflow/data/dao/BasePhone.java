package com.ngdesk.workflow.data.dao;

import com.fasterxml.jackson.annotation.JsonProperty;

public class BasePhone {

	@JsonProperty("DIAL_CODE")
	private String dialCode;

	@JsonProperty("COUNTRY_FLAG")
	private String countryFlag;

	@JsonProperty("COUNTRY_CODE")
	private String countryCode;

	@JsonProperty("PHONE_NUMBER")
	private String phoneNumber;

	public BasePhone() {

	}

	public BasePhone(String dialCode, String countryFlag, String countryCode, String phoneNumber) {
		super();
		this.dialCode = dialCode;
		this.countryFlag = countryFlag;
		this.countryCode = countryCode;
		this.phoneNumber = phoneNumber;
	}

	public String getDialCode() {
		return dialCode;
	}

	public void setDialCode(String dialCode) {
		this.dialCode = dialCode;
	}

	public String getCountryFlag() {
		return countryFlag;
	}

	public void setCountryFlag(String countryFlag) {
		this.countryFlag = countryFlag;
	}

	public String getCountryCode() {
		return countryCode;
	}

	public void setCountryCode(String countryCode) {
		this.countryCode = countryCode;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

}
