package com.ngdesk.customers;

import java.sql.Timestamp;
import java.util.Date;

import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Pattern.Flag;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ngdesk.users.Phone;

@JsonInclude(Include.NON_NULL)
public class Customer {
	@JsonProperty("EMAIL_ADDRESS")
	@NotEmpty(message = "EMAIL_ADDRESS_NOT_EMPTY")
	@Email(flags = Flag.CASE_INSENSITIVE, message = "EMAIL_INVALID")
	@Pattern(regexp = "^(.+)@(.+)\\.(.+)$", message = "EMAIL_INVALID")
	private String emailAddress;

	@JsonProperty("EMAIL_VERIFIED")
	private boolean emailVerified;

	@JsonProperty("PASSWORD")
	@NotEmpty(message = "PASSWORD_NOT_EMPTY")
	@Pattern(regexp = "^(?=.*?[A-Z])(?=.*?[_#?!@$%^&*-]).{8,100}$", message = "PASSWORD_INVALID")
	private String password;

	@JsonProperty("FIRST_NAME")
	@NotEmpty(message = "FIRST_NAME_NOT_EMPTY")
	private String firstName;

	@JsonProperty("LAST_NAME")
	@NotEmpty(message = "FIRST_NAME_NOT_EMPTY")
	private String lastName;

	@JsonProperty("PHONE_NUMBER")
	@NotNull(message = "PHONE_NOT_NULL")
	@Valid
	private Phone phone;

	@JsonProperty("USER_UUID")
	private String userUUID;

	@JsonProperty("STATUS")
	private String status;

	@JsonProperty("DISABLED")
	private boolean disabled;

	@JsonProperty("LANGUAGE")
	private String language;

	@JsonProperty("LAST_SEEN")
	private String lastSeen;

	@JsonProperty("ROLE")
	private String role;

	@JsonProperty("LOGIN_ATTEMPTS")
	private int loginAttempts;

	@JsonProperty("DATE_CREATED")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
	private Date dateCreated;

	@JsonProperty("DATE_UPDATED")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
	private Date lastUpdated;

	@JsonProperty("DELETED")
	private boolean deleted;

	public Customer() {

	}

	public Customer(
			@NotEmpty(message = "EMAIL_ADDRESS_NOT_EMPTY") @Email(flags = Flag.CASE_INSENSITIVE, message = "EMAIL_INVALID") @Pattern(regexp = "^(.+)@(.+)\\.(.+)$", message = "EMAIL_INVALID") String emailAddress,
			boolean emailVerified,
			@NotEmpty(message = "PASSWORD_NOT_EMPTY") @Pattern(regexp = "^(?=.*?[A-Z])(?=.*?[_#?!@$%^&*-]).{8,100}$", message = "PASSWORD_INVALID") String password,
			@NotEmpty(message = "FIRST_NAME_NOT_EMPTY") String firstName,
			@NotEmpty(message = "FIRST_NAME_NOT_EMPTY") String lastName,
			@NotNull(message = "PHONE_NOT_NULL") @Valid Phone phone, String userUUID, String status, boolean disabled,
			String language, String lastSeen, String role, int loginAttempts, Date dateCreated, Date lastUpdated,
			boolean deleted) {
		super();
		this.emailAddress = emailAddress;
		this.emailVerified = emailVerified;
		this.password = password;
		this.firstName = firstName;
		this.lastName = lastName;
		this.phone = phone;
		this.userUUID = userUUID;
		this.status = status;
		this.disabled = disabled;
		this.language = language;
		this.lastSeen = lastSeen;
		this.role = role;
		this.loginAttempts = loginAttempts;
		this.dateCreated = dateCreated;
		this.lastUpdated = lastUpdated;
		this.deleted = deleted;
	}

	public String getEmailAddress() {
		return emailAddress;
	}

	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}

	public boolean isEmailVerified() {
		return emailVerified;
	}

	public void setEmailVerified(boolean emailVerified) {
		this.emailVerified = emailVerified;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
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

	public Phone getPhone() {
		return phone;
	}

	public void setPhone(Phone phone) {
		this.phone = phone;
	}

	public String getUserUUID() {
		return userUUID;
	}

	public void setUserUUID(String userUUID) {
		this.userUUID = userUUID;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public boolean isDisabled() {
		return disabled;
	}

	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public String getLastSeen() {
		return lastSeen;
	}

	public void setLastSeen(String lastSeen) {
		this.lastSeen = lastSeen;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public int getLoginAttempts() {
		return loginAttempts;
	}

	public void setLoginAttempts(int loginAttempts) {
		this.loginAttempts = loginAttempts;
	}

	public Date getDateCreated() {
		return dateCreated;
	}

	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}

	public Date getLastUpdated() {
		return lastUpdated;
	}

	public void setLastUpdated(Date lastUpdated) {
		this.lastUpdated = lastUpdated;
	}

	public boolean isDeleted() {
		return deleted;
	}

	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}

}
