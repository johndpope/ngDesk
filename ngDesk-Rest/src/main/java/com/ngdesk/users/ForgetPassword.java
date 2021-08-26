package com.ngdesk.users;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.validation.constraints.Pattern.Flag;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ForgetPassword {
	@JsonProperty("EMAIL_ADDRESS")
	@NotNull(message = "EMAIL_ADDRESS_NOT_NULL")
	@Size(min = 1, message = "EMAIL_ADDRESS_NOT_EMPTY")
	@Email(flags = Flag.CASE_INSENSITIVE, message = "EMAIL_INVALID")
	private String emailAddress;

	public ForgetPassword() {

	}

	public ForgetPassword(
			@NotNull(message = "EMAIL_ADDRESS_NOT_NULL") @Size(min = 1, message = "EMAIL_ADDRESS_NOT_EMPTY") @Email(flags = Flag.CASE_INSENSITIVE, message = "EMAIL_INVALID") String emailAddress) {
		super();
		this.emailAddress = emailAddress;
	}

	public String getEmailAddress() {
		return emailAddress;
	}

	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}

}
