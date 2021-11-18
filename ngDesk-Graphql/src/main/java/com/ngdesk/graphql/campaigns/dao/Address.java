package com.ngdesk.graphql.campaigns.dao;

import org.springframework.data.mongodb.core.mapping.Field;

public class Address {

	@Field("COMPANY_NAME")
	private String companyName;

	@Field("ADDRESS_1")
	private String address1;

	@Field("ADDRESS_2")
	private String address2;

	@Field("CITY")
	private String city;

	@Field("STATE")
	private String state;

	@Field("ZIP_CODE")
	private String zipCode;

	@Field("COUNTRY")
	private String country;

	@Field("PHONE")
	private String phone;

	public Address() {

	}

	public Address(String companyName, String address1, String address2, String city, String state, String zipCode,
			String country, String phone) {
		super();
		this.companyName = companyName;
		this.address1 = address1;
		this.address2 = address2;
		this.city = city;
		this.state = state;
		this.zipCode = zipCode;
		this.country = country;
		this.phone = phone;
	}

	public String getCompanyName() {
		return companyName;
	}

	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}

	public String getAddress1() {
		return address1;
	}

	public void setAddress1(String address1) {
		this.address1 = address1;
	}

	public String getAddress2() {
		return address2;
	}

	public void setAddress2(String address2) {
		this.address2 = address2;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getZipCode() {
		return zipCode;
	}

	public void setZipCode(String zipCode) {
		this.zipCode = zipCode;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

}
