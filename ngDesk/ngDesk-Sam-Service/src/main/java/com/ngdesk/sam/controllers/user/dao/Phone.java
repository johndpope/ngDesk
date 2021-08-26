package com.ngdesk.sam.controllers.user.dao;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Phone {

	@JsonProperty("COUNTRY_CODE")
	String countryCode;

	@JsonProperty("DIAL_CODE")
	String dialCode;

	@JsonProperty("PHONE_NUMBER")
	String phoneNumber;

	@JsonProperty("COUNTRY_FLAG")
	String countryFlag;

	public Phone() {

	}

	public Phone(String countryCode, String dialCode, String phoneNumber, String countryFlag) {
		super();
		this.countryCode = countryCode;
		this.dialCode = dialCode;
		this.phoneNumber = phoneNumber;
		this.countryFlag = countryFlag;
	}

	public String getCountryCode() {
		return countryCode;
	}

	public void setCountryCode(String countryCode) {
		this.countryCode = countryCode;
	}

	public String getDialCode() {
		return dialCode;
	}

	public void setDialCode(String dialCode) {
		this.dialCode = dialCode;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public String getCountryFlag() {
		return countryFlag;
	}

	public void setCountryFlag(String countryFlag) {
		this.countryFlag = countryFlag;
	}

}
