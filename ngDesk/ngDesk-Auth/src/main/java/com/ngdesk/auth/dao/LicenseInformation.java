package com.ngdesk.auth.dao;

public class LicenseInformation {

	private String licenseKey;

	private Integer noOfUsers;

	public LicenseInformation() {

	}

	public LicenseInformation(String licenseKey, Integer noOfUsers) {
		super();
		this.licenseKey = licenseKey;
		this.noOfUsers = noOfUsers;
	}

	public String getLicenseKey() {
		return licenseKey;
	}

	public void setLicenseKey(String licenseKey) {
		this.licenseKey = licenseKey;
	}

	public Integer getNoOfUsers() {
		return noOfUsers;
	}

	public void setNoOfUsers(Integer noOfUsers) {
		this.noOfUsers = noOfUsers;
	}

}
