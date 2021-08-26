package com.ngdesk.company.onpremise.dao;

public class PhoneOnPremise {

	private String countryCode;

	private String dialCode;

	private String phoneNumber;

	private String countryFlag;

	public PhoneOnPremise() {

	}

	public PhoneOnPremise(String countryCode, String dialCode, String phoneNumber, String countryFlag) {
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