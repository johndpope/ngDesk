package com.ngdesk.company.module.dao;

import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Sender {
	
	@Field("FIRST_NAME")
	@JsonProperty("FIRST_NAME")
	private String firstName;

	@Field("LAST_NAME")
	@JsonProperty("LAST_NAME")
	private String lastName;

	@Field("UUID")
	@JsonProperty("UUID")
	private String sender;

	@Field("ROLE")
	@JsonProperty("ROLE")
	private String role;

	public Sender() {

	}

	public Sender(String firstName, String lastName, String sender, String role) {
		super();
		this.firstName = firstName;
		this.lastName = lastName;
		this.sender = sender;
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

	public String getSender() {
		return sender;
	}

	public void setSender(String sender) {
		this.sender = sender;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

}
