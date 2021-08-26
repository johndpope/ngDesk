package com.ngdesk.commons.models;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

public class User {

	@JsonProperty("USERNAME")
	private String username;

	@JsonProperty("FIRST_NAME")
	private String firstName;

	@JsonProperty("LAST_NAME")
	private String lastName;

	@JsonProperty("ROLE")
	private String role;

	@JsonProperty("EMAIL_ADDRESS")
	private String emailAddress;

	@JsonProperty("COMPANY_ID")
	private String companyId;

	@JsonProperty("COMPANY_SUBDOMAIN")
	private String companySubdomain;

	@JsonProperty("USER_ID")
	private String userId;

	@JsonProperty("LANGUAGE")
	private String language;

	@JsonProperty("USER_UUID")
	private String userUuid;

	@JsonProperty("USER_ATTRIBUTES")
	private Map<String, Object> attributes;

	public User() {

	}

	public User(String username, String firstName, String lastName, String role, String emailAddress, String companyId,
			String companySubdomain, String userId, String language, String userUuid, Map<String, Object> attributes) {
		super();
		this.username = username;
		this.firstName = firstName;
		this.lastName = lastName;
		this.role = role;
		this.emailAddress = emailAddress;
		this.companyId = companyId;
		this.companySubdomain = companySubdomain;
		this.userId = userId;
		this.language = language;
		this.userUuid = userUuid;
		this.attributes = attributes;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
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

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public String getEmailAddress() {
		return emailAddress;
	}

	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}

	public String getCompanyId() {
		return companyId;
	}

	public void setCompanyId(String companyId) {
		this.companyId = companyId;
	}

	public String getCompanySubdomain() {
		return companySubdomain;
	}

	public void setCompanySubdomain(String companySubdomain) {
		this.companySubdomain = companySubdomain;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public String getUserUuid() {
		return userUuid;
	}

	public void setUserUuid(String userUuid) {
		this.userUuid = userUuid;
	}

	public Map<String, Object> getAttributes() {
		return attributes;
	}

	public void setAttributes(Map<String, Object> attributes) {
		this.attributes = attributes;
	}

}
