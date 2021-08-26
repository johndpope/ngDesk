package com.ngdesk.module.dao;

import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Phone {

	@JsonProperty("COUNTRY_CODE")
	@Field("COUNTRY_CODE")
	private String countryCode;

	@JsonProperty("DIAL_CODE")
	@Field("DIAL_CODE")
	private String dialCode;

	@JsonProperty("PHONE_NUMBER")
	@Field("PHONE_NUMBER")
	private String phoneNumber;

	@JsonProperty("COUNTRY_FLAG")
	@Field("COUNTRY_FLAG")
	private String countryFlag;

	public Phone() {
		super();
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
