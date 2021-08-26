package com.ngdesk.users;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Phone {

	@JsonProperty("COUNTRY_CODE")
	@NotNull(message = "COUNTRY_CODE_MISSING")
	@Size(min = 2, max = 2, message = "INVALID_COUNTRY_CODE")
	String countryCode;

	@JsonProperty("DIAL_CODE")
	@NotNull(message = "DIAL_CODE_MISSING")
	String dialCode;

	@JsonProperty("PHONE_NUMBER")
	@Size(min = 6, max = 14, message = "PHONE_INVALID")
	@Pattern(regexp = "^\\+?[0-9]*$", message = "PHONE_NUMBER_INVALID")
	@NotNull(message = "PHONE_NOT_NULL")
	String phoneNumber;

	@JsonProperty("COUNTRY_FLAG")
	@NotEmpty(message = "COUNTRY_FLAG_NULL")
	String countryFlag;

	public Phone() {

	}

	public Phone(
			@NotNull(message = "COUNTRY_CODE_MISSING") @Size(min = 2, max = 2, message = "INVALID_COUNTRY_CODE") String countryCode,
			@NotNull(message = "DIAL_CODE_MISSING") String dialCode,
			@Size(min = 6, max = 14, message = "PHONE_INVALID") @NotNull(message = "PHONE_NOT_NULL") String phoneNumber,
			@NotNull(message = "COUNTRY_FLAG_NULL") String countryFlag) {
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
