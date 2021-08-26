package com.ngdesk.integration.zoom.dao;

import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ZoomUserInformation {

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

	@JsonProperty("PERSONAL_MEETING_URL")
	@Field("PERSONAL_MEETING_URL")
	private String personalMeetingUrl;

	@JsonProperty("COMPANY_NAME")
	@Field("COMPANY_NAME")
	private String companyName;

	@JsonProperty("ROLE_NAME")
	@Field("ROLE_NAME")
	private String roleName;

	@JsonProperty("EMAIL_ADDRESS")
	@Field("EMAIL_ADDRESS")
	private String emailAddress;

	public ZoomUserInformation(String userId, String accountId, String firstName, String lastName,
			String personalMeetingUrl, String companyName, String roleName, String emailAddress) {
		super();
		this.userId = userId;
		this.accountId = accountId;
		this.firstName = firstName;
		this.lastName = lastName;
		this.personalMeetingUrl = personalMeetingUrl;
		this.companyName = companyName;
		this.roleName = roleName;
		this.emailAddress = emailAddress;
	}

	public ZoomUserInformation() {
		super();
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

	public String getPersonalMeetingUrl() {
		return personalMeetingUrl;
	}

	public void setPersonalMeetingUrl(String personalMeetingUrl) {
		this.personalMeetingUrl = personalMeetingUrl;
	}

	public String getCompanyName() {
		return companyName;
	}

	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}

	public String getRoleName() {
		return roleName;
	}

	public void setRoleName(String roleName) {
		this.roleName = roleName;
	}

	public String getEmailAddress() {
		return emailAddress;
	}

	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}

}
