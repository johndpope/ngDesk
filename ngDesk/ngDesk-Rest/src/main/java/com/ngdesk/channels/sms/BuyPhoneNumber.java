package com.ngdesk.channels.sms;

import javax.validation.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonProperty;

public class BuyPhoneNumber {

	@JsonProperty("PHONE_NUMBER")
	@NotEmpty(message = "PHONE_NUMBER_NOT_NULL")
	private String phoneNumber;

	public BuyPhoneNumber() {
	}

	public BuyPhoneNumber(@NotEmpty(message = "PHONE_NUMBER_NOT_NULL") String phoneNumber) {
		super();
		this.phoneNumber = phoneNumber;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

}
