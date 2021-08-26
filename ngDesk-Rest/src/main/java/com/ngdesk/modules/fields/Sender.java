package com.ngdesk.modules.fields;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Sender {
	@JsonProperty("FIRST_NAME")
	private String firstName;

	@JsonProperty("LAST_NAME")
	private String lastName;

	@JsonProperty("UUID")
	private String sender;

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
