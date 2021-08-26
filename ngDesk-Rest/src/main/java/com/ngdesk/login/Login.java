package com.ngdesk.login;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Pattern.Flag;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(Include.NON_NULL)
public class Login {

	@JsonProperty("EMAIL_ADDRESS")
	@NotNull(message = "EMAIL_ADDRESS_NOT_NULL")
	@Email(flags = Flag.CASE_INSENSITIVE, message = "EMAIL_INVALID")
	@Pattern(regexp = "^(.+)@(.+)\\.(.+)$", message = "EMAIL_INVALID")
	private String emailAddress;

	@JsonProperty("PASSWORD")
	@NotNull(message = "PASSWORD_NOT_NULL")
	private String password;

	@JsonProperty("SUBDOMAIN")
	private String companySubdomain;

	public Login() {

	}

	public Login(
			@NotNull(message = "EMAIL_ADDRESS_NOT_NULL") @Email(flags = Flag.CASE_INSENSITIVE, message = "EMAIL_INVALID") @Pattern(regexp = "^(.+)@(.+)\\.(.+)$", message = "EMAIL_INVALID") String emailAddress,
			@NotNull(message = "PASSWORD_NOT_NULL") String password, String companySubdomain) {
		super();
		this.emailAddress = emailAddress;
		this.password = password;
		this.companySubdomain = companySubdomain;
	}

	public String getEmailAddress() {
		return emailAddress;
	}

	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getCompanySubdomain() {
		return companySubdomain;
	}

	public void setCompanySubdomain(String companySubdomain) {
		this.companySubdomain = companySubdomain;
	}

}
