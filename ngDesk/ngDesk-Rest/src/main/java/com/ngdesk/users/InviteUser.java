package com.ngdesk.users;

import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Pattern.Flag;

import com.fasterxml.jackson.annotation.JsonProperty;

public class InviteUser {

	@JsonProperty("FIRST_NAME")
	@NotBlank(message = "FIRST_NAME_NOT_NULL")
	private String firstName;

	@JsonProperty("LAST_NAME")
	@NotNull(message = "LAST_NAME_NOT_NULL")
	private String lastName;

	@JsonProperty("EMAIL_ADDRESS")
	@NotNull(message = "EMAIL_ADDRESS_NOT_NULL")
	@Email(flags = Flag.CASE_INSENSITIVE, message = "EMAIL_INVALID")
	@Pattern(regexp = "^(.+)@(.+)\\.(.+)$", message = "EMAIL_INVALID")
	private String emailAddress;

	@JsonProperty("ROLE")
	@NotNull(message = "ROLE_NOT_NULL")
	private String role;
	
	@JsonProperty("PHONE_NUMBER")
	@Valid
	private Phone phone;

	public InviteUser() {

	}

	
	public InviteUser(@NotBlank(message = "FIRST_NAME_NOT_NULL") String firstName,
			@NotNull(message = "LAST_NAME_NOT_NULL") String lastName,
			@NotNull(message = "EMAIL_ADDRESS_NOT_NULL") @Email(flags = Flag.CASE_INSENSITIVE, message = "EMAIL_INVALID") @Pattern(regexp = "^(.+)@(.+)\\.(.+)$", message = "EMAIL_INVALID") String emailAddress,
			@NotNull(message = "ROLE_NOT_NULL") String role, @Valid Phone phone) {
		super();
		this.firstName = firstName;
		this.lastName = lastName;
		this.emailAddress = emailAddress;
		this.role = role;
		this.phone = phone;
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


	public Phone getPhone() {
		return phone;
	}


	public void setPhone(Phone phone) {
		this.phone = phone;
	}

}
