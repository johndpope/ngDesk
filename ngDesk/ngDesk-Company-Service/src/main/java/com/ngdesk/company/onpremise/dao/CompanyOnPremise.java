package com.ngdesk.company.onpremise.dao;

import java.util.Date;

import org.springframework.data.annotation.Id;

import com.ngdesk.commons.annotations.CustomNotEmpty;

public class CompanyOnPremise {

	@Id
	private String id;

	@CustomNotEmpty(message = "DAO_VARIABLE_REQUIRED", values = { "LICENSE_KEY" })
	private String licenseKey;

	private Date dateCreated;

	private String subdomain;

	private String name;

	private String emailAddress;

	private PhoneOnPremise phone;

	public CompanyOnPremise() {

	}

	public CompanyOnPremise(String id, String licenseKey, Date dateCreated, String subdomain, String name,
			String emailAddress, PhoneOnPremise phone) {
		super();
		this.id = id;
		this.licenseKey = licenseKey;
		this.dateCreated = dateCreated;
		this.subdomain = subdomain;
		this.name = name;
		this.emailAddress = emailAddress;
		this.phone = phone;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getLicenseKey() {
		return licenseKey;
	}

	public void setLicenseKey(String licenseKey) {
		this.licenseKey = licenseKey;
	}

	public Date getDateCreated() {
		return dateCreated;
	}

	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}

	public String getSubdomain() {
		return subdomain;
	}

	public void setSubdomain(String subdomain) {
		this.subdomain = subdomain;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEmailAddress() {
		return emailAddress;
	}

	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}

	public PhoneOnPremise getPhone() {
		return phone;
	}

	public void setPhone(PhoneOnPremise phone) {
		this.phone = phone;
	}

}
