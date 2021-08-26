package com.ngdesk.integration.docusign;

import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DocusignUserInformation {

	@JsonProperty("USER_ID")
	@Field("USER_ID")
	private String userId;

	@JsonProperty("ACCOUNT_ID")
	@Field("ACCOUNT_ID")
	private String accountId;

	@JsonProperty("FIRST_NAME")
	@Field("FIRST_NAME")
	private String firstName;

	@JsonProperty("LAST_NAME")
	@Field("LAST_NAME")
	private String lastName;

	@JsonProperty("ACCOUNT_NAME")
	@Field("ACCOUNT_NAME")
	private String accountName;

	@JsonProperty("EMAIL_ADDRESS")
	@Field("EMAIL_ADDRESS")
	private String emailAddress;

	@JsonProperty("FULL_NAME")
	@Field("FULL_NAME")
	private String fullName;

	@JsonProperty("BASE_PATH")
	@Field("BASE_PATH")
	private String basePath;

	public DocusignUserInformation() {

	}

	public DocusignUserInformation(String userId, String accountId, String firstName, String lastName,
			String accountName, String emailAddress, String fullName, String basePath) {
		super();
		this.userId = userId;
		this.accountId = accountId;
		this.firstName = firstName;
		this.lastName = lastName;
		this.accountName = accountName;
		this.emailAddress = emailAddress;
		this.fullName = fullName;
		this.basePath = basePath;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getAccountId() {
		return accountId;
	}

	public void setAccountId(String accountId) {
		this.accountId = accountId;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getAccountName() {
		return accountName;
	}

	public void setAccountName(String accountName) {
		this.accountName = accountName;
	}

	public String getEmailAddress() {
		return emailAddress;
	}

	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public String getBasePath() {
		return basePath;
	}

	public void setBasePath(String basePath) {
		this.basePath = basePath;
	}
}
