package com.ngdesk.company.onpremise.dao;

import java.util.Date;

import org.springframework.data.annotation.Id;

import com.ngdesk.commons.annotations.CustomNotEmpty;

public class CompanyOnPremiseAudit {

	@Id
	private String id;

	@CustomNotEmpty(message = "DAO_VARIABLE_REQUIRED", values = { "NO_OF_USERS" })
	private Integer noOfUsers;
	
	@CustomNotEmpty(message = "DAO_VARIABLE_REQUIRED", values = { "LICENSE_KEY" })
	private String licenseKey;

	private Date dateCreated;

	public CompanyOnPremiseAudit() {

	}

	public CompanyOnPremiseAudit(String id, Integer noOfUsers, String licenseKey, Date dateCreated) {
		this.id = id;
		this.noOfUsers = noOfUsers;
		this.licenseKey = licenseKey;
		this.dateCreated = dateCreated;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Integer getNoOfUsers() {
		return noOfUsers;
	}

	public void setNoOfUsers(Integer noOfUsers) {
		this.noOfUsers = noOfUsers;
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

}
