package com.ngdesk.company.dao;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ngdesk.commons.annotations.CustomNotEmpty;

public class Phone {

	@JsonProperty("COUNTRY_CODE")
	@Size(min = 2, max = 2, message = "INVALID_COUNTRY_CODE")
	private String countryCode;

	@JsonProperty("DIAL_CODE")
	@CustomNotEmpty(message = "DAO_VARIABLE_REQUIRED", values = { "DIAL_CODE" })
	private String dialCode;

	@JsonProperty("PHONE_NUMBER")
	@CustomNotEmpty(message = "DAO_VARIABLE_REQUIRED", values = { "PHONE_NUMBER" })
	@Size(min = 6, max = 14, message = "PHONE_SIZE_INVALID")
	@Pattern(regexp = "^\\+?[0-9]*$", message = "PHONE_NUMBER_INVALID")
	private String phoneNumber;

	@JsonProperty("COUNTRY_FLAG")
	@CustomNotEmpty(message = "DAO_VARIABLE_REQUIRED", values = { "COUNTRY_FLAG" })
	private String countryFlag;

	public Phone() {
		super();
	}

	public Phone(@Size(min = 2, max = 2, message = "INVALID_COUNTRY_CODE") String countryCode, String dialCode,
			@Size(min = 6, max = 14, message = "PHONE_SIZE_INVALID") @Pattern(regexp = "^\\+?[0-9]*$", message = "PHONE_NUMBER_INVALID") String phoneNumber,
			String countryFlag) {
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
