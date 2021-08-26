package com.ngdesk.customers;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Pattern.Flag;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CustomerToUser {

	@JsonProperty("EMAIL_ADDRESS")
	@NotEmpty(message = "CUSTOMER_EMPTY")
	@Email(flags = Flag.CASE_INSENSITIVE, message = "EMAIL_INVALID")
	@Pattern(regexp = "^(.+)@(.+)\\.(.+)$", message = "EMAIL_INVALID")
	private String emailAddress;

	@JsonProperty("ROLE")
	@NotNull(message = "ROLE_NOT_NULL")
	private String role;

	public CustomerToUser() {

	}

	public CustomerToUser(
			@NotEmpty(message = "CUSTOMER_EMPTY") @Email(flags = Flag.CASE_INSENSITIVE, message = "EMAIL_INVALID") @Pattern(regexp = "^(.+)@(.+)\\.(.+)$", message = "EMAIL_INVALID") String emailAddress,
			@NotNull(message = "ROLE_NOT_NULL") String role) {
		super();
		this.emailAddress = emailAddress;
		this.role = role;
	}

	public String getEmailAddress() {
		return emailAddress;
	}

	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

}
