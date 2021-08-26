package com.ngdesk.channels.sms;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PhoneNumber {

	@JsonProperty("DIAL_CODE")
	@Size(min = 2, max = 4, message = "DIAL_CODE_INVALID")
	@Pattern(regexp = "^\\+?[0-9]*$", message = "DIAL_CODE_INVALID")
	@NotNull(message = "DIAL_CODE_MISSING")
	String dialCode;

	@JsonProperty("PHONE_NUMBER")
	@Size(min = 6, max = 14, message = "PHONE_INVALID")
	@Pattern(regexp = "^\\+?[0-9]*$", message = "PHONE_NUMBER_INVALID")
	@NotNull(message = "PHONE_NOT_NULL")
	String phoneNumber;

	public PhoneNumber() {
	}

	public PhoneNumber(
			@Size(min = 2, max = 4, message = "DIAL_CODE_INVALID") @Pattern(regexp = "^\\+?[0-9]*$", message = "DIAL_CODE_INVALID") @NotNull(message = "DIAL_CODE_MISSING") String dialCode,
			@Size(min = 6, max = 14, message = "PHONE_INVALID") @Pattern(regexp = "^\\+?[0-9]*$", message = "PHONE_NUMBER_INVALID") @NotNull(message = "PHONE_NOT_NULL") String phoneNumber) {
		super();
		this.dialCode = dialCode;
		this.phoneNumber = phoneNumber;
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

}
