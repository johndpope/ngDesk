package com.ngdesk.data.dao;

import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Sender {

	@JsonProperty("FIRST_NAME")
	@Field("FIRST_NAME")
	private String firstName;

	@JsonProperty("LAST_NAME")
	@Field("LAST_NAME")
	private String lastName;

	@JsonProperty("USER_UUID")
	@Field("USER_UUID")
	private String userUuid;

	@JsonProperty("ROLE")
	@Field("ROLE")
	private String role;

	public Sender() {

	}

	public Sender(String firstName, String lastName, String userUuid, String role) {
		super();
		this.firstName = firstName;
		this.lastName = lastName;
		this.userUuid = userUuid;
		this.role = role;
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

	public String getUserUuid() {
		return userUuid;
	}

	public void setUserUuid(String userUuid) {
		this.userUuid = userUuid;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

}
