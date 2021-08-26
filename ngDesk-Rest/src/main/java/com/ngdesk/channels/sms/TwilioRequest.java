package com.ngdesk.channels.sms;

import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import javax.validation.constraints.Pattern.Flag;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ngdesk.annotations.UniqueCompany;

public class TwilioRequest {
	@JsonProperty("COMPANY_SUBDOMAIN")
	@NotNull(message = "COMPANY_SUBDOMAIN_NOT_NULL")
	@Size(min = 1, max = 64, message = "COMPANY_SUBDOMAIN_NOT_EMPTY")
	@Pattern(regexp = "([A-Za-z0-9\\-]+)", message = "INVALID_SUBDOMAIN")
	@UniqueCompany(message = "COMPANY_NOT_UNIQUE")
	private String companySubdomain;

	@JsonProperty("EMAIL_ADDRESS")
	@NotNull(message = "EMAIL_ADDRESS_NOT_NULL")
	@Size(min = 1, max = 255, message = "EMAIL_ADDRESS_NOT_EMPTY")
	@Email(flags = Flag.CASE_INSENSITIVE, message = "EMAIL_INVALID")
	private String emailAddress;

	@JsonProperty("FIRST_NAME")
	@NotNull(message = "FIRST_NAME_NOT_NULL")
	@Size(min = 1, message = "FIRST_NAME_NOT_EMPTY")
	@Pattern(regexp = "^[A-Za-z0-9\\-_ ]*$", message = "FIRST_NAME_INVALID")
	private String firstName;

	@JsonProperty("LAST_NAME")
	@NotNull(message = "LAST_NAME_NOT_NULL")
	@Size(min = 1, message = "LAST_NAME_NOT_EMPTY")
	@Pattern(regexp = "^[A-Za-z0-9\\-_ ]*$", message = "LAST_NAME_INVALID")
	private String lastName;

	@JsonProperty("PHONE_NUMBER")
	@NotNull(message = "PHONE_NOT_NULL")
	private String phoneNumber;

	@JsonProperty("COUNTRY")
	@NotNull(message = "COUNTRY_NOT_NULL")
	@Valid
	private Country country;

	public TwilioRequest() {
	}

	public TwilioRequest(
			@NotNull(message = "COMPANY_SUBDOMAIN_NOT_NULL") @Size(min = 1, max = 64, message = "COMPANY_SUBDOMAIN_NOT_EMPTY") @Pattern(regexp = "([A-Za-z0-9\\-]+)", message = "INVALID_SUBDOMAIN") String companySubdomain,
			@NotNull(message = "EMAIL_ADDRESS_NOT_NULL") @Size(min = 1, max = 255, message = "EMAIL_ADDRESS_NOT_EMPTY") @Email(flags = Flag.CASE_INSENSITIVE, message = "EMAIL_INVALID") String emailAddress,
			@NotNull(message = "FIRST_NAME_NOT_NULL") @Size(min = 1, message = "FIRST_NAME_NOT_EMPTY") @Pattern(regexp = "^[A-Za-z0-9\\-_ ]*$", message = "FIRST_NAME_INVALID") String firstName,
			@NotNull(message = "LAST_NAME_NOT_NULL") @Size(min = 1, message = "LAST_NAME_NOT_EMPTY") @Pattern(regexp = "^[A-Za-z0-9\\-_ ]*$", message = "LAST_NAME_INVALID") String lastName,
			@NotNull(message = "PHONE_NOT_NULL") String phoneNumber,
			@NotNull(message = "COUNTRY_NOT_NULL") @Valid Country country) {
		super();
		this.companySubdomain = companySubdomain;
		this.emailAddress = emailAddress;
		this.firstName = firstName;
		this.lastName = lastName;
		this.phoneNumber = phoneNumber;
		this.country = country;
	}

	public String getCompanySubdomain() {
		return companySubdomain;
	}

	public void setCompanySubdomain(String companySubdomain) {
		this.companySubdomain = companySubdomain;
	}

	public String getEmailAddress() {
		return emailAddress;
	}

	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
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

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public Country getCountry() {
		return country;
	}

	public void setCountry(Country country) {
		this.country = country;
	}

}
