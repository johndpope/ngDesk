package com.ngdesk.sam.controllers.user.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Pattern.Flag;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

public class User {

	@Id
	@JsonProperty("DATA_ID")
	private String dataId;

	@JsonProperty("USERNAME")
	private String username;

	@Field("FIRST_NAME")
	@JsonProperty("FIRST_NAME")
	@NotBlank(message = "FIRST_NAME_NOT_NULL")
	private String firstName;

	@Field("LAST_NAME")
	@JsonProperty("LAST_NAME")
	@NotNull(message = "LAST_NAME_NOT_NULL")
	private String lastName;

	@Field("ROLE")
	@JsonProperty("ROLE")
	@NotNull(message = "ROLE_NOT_NULL")
	private String role;

	@Field("EMAIL_ADDRESS")
	@JsonProperty("EMAIL_ADDRESS")
	@NotNull(message = "EMAIL_ADDRESS_NOT_NULL")
	@Email(flags = Flag.CASE_INSENSITIVE, message = "EMAIL_INVALID")
	@Pattern(regexp = "^(.+)@(.+)\\.(.+)$", message = "EMAIL_INVALID")
	private String emailAddress;

	@JsonProperty("COMPANY_ID")
	private String companyId;

	@JsonProperty("COMPANY_SUBDOMAIN")
	private String companySubdomain;

	@JsonProperty("LANGUAGE")
	@Field("LANGUAGE")
	private String language;

	@Field("USER_UUID")
	@JsonProperty("USER_UUID")
	private String userUuid;

	@JsonProperty("USER_ATTRIBUTES")
	private Map<String, Object> attributes;

	@Field("IS_LOGIN_ALLOWED")
	@JsonProperty("IS_LOGIN_ALLOWED")
	private boolean isLoginAllowed;

	@Field("PASSWORD")
	@JsonProperty("PASSWORD")
	private String password;

	@Field("LOGIN_ATTEMPTS")
	@JsonProperty("LOGIN_ATTEMPTS")
	private Integer loginAttempts;

	@Field("DELETED")
	@JsonProperty("DELETED")
	private boolean deleted;

	@JsonProperty("DATE_CREATED")
	@Field("DATE_CREATED")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
	private String dateCreated;

	@JsonProperty("DATE_UPDATED")
	@Field("DATE_UPDATED")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
	private String dateUpdated;

	@Field("EMAIL_VERIFIED")
	@JsonProperty("EMAIL_VERIFIED")
	private boolean emailVerified;

	@Field("PHONE_NUMBER")
	@JsonProperty("PHONE_NUMBER")
	private Phone phoneNumber;

	@Field("DISABLED")
	@JsonProperty("DISABLED")
	private boolean disabled;

	@Field("NOTIFICATION_SOUND")
	@JsonProperty("NOTIFICATION_SOUND")
	private String notificationSound;

	@Field("STATUS")
	@JsonProperty("STATUS")
	private String status;

	@Field("DEFAULT_CONTACT_METHOD")
	@JsonProperty("DEFAULT_CONTACT_METHOD")
	private String defaultContactMethod;

	@Field("LAST_SEEN")
	@JsonProperty("LAST_SEEN")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
	private String lastSeen;

	@JsonProperty("LAST_UPDATED_BY")
	@Field("LAST_UPDATED_BY")
	private String lastUpdated;

	@Field("INVITE_ACCEPTED")
	@JsonProperty("INVITE_ACCEPTED")
	private boolean inviteAccepted;

	@Field("SUBSCRIPTION_ON_MARKETING_EMAIL")
	@JsonProperty("SUBSCRIPTION_ON_MARKETING_EMAIL")
	private boolean subscriptionOnMarketingEmail;

	@Field("TEAMS")
	@JsonProperty("TEAMS")
	private List<String> teams = new ArrayList<String>();

	@Field("ACCOUNT")
	@JsonProperty("ACCOUNT")
	private String account; 

	public User() {

	}

	public User(String dataId, String username, @NotBlank(message = "FIRST_NAME_NOT_NULL") String firstName,
			@NotNull(message = "LAST_NAME_NOT_NULL") String lastName, @NotNull(message = "ROLE_NOT_NULL") String role,
			@NotNull(message = "EMAIL_ADDRESS_NOT_NULL") @Email(flags = Flag.CASE_INSENSITIVE, message = "EMAIL_INVALID") @Pattern(regexp = "^(.+)@(.+)\\.(.+)$", message = "EMAIL_INVALID") String emailAddress,
			String companyId, String companySubdomain, String language, String userUuid, Map<String, Object> attributes,
			boolean isLoginAllowed, String password, Integer loginAttempts, boolean deleted, String dateCreated,
			String dateUpdated, boolean emailVerified, Phone phoneNumber, boolean disabled, String notificationSound,
			String status, String defaultContactMethod, String lastSeen, String lastUpdated, boolean inviteAccepted,
			boolean subscriptionOnMarketingEmail, List<String> teams, String account) {
		super();
		this.dataId = dataId;
		this.username = username;
		this.firstName = firstName;
		this.lastName = lastName;
		this.role = role;
		this.emailAddress = emailAddress;
		this.companyId = companyId;
		this.companySubdomain = companySubdomain;
		this.language = language;
		this.userUuid = userUuid;
		this.attributes = attributes;
		this.isLoginAllowed = isLoginAllowed;
		this.password = password;
		this.loginAttempts = loginAttempts;
		this.deleted = deleted;
		this.dateCreated = dateCreated;
		this.dateUpdated = dateUpdated;
		this.emailVerified = emailVerified;
		this.phoneNumber = phoneNumber;
		this.disabled = disabled;
		this.notificationSound = notificationSound;
		this.status = status;
		this.defaultContactMethod = defaultContactMethod;
		this.lastSeen = lastSeen;
		this.lastUpdated = lastUpdated;
		this.inviteAccepted = inviteAccepted;
		this.subscriptionOnMarketingEmail = subscriptionOnMarketingEmail;
		this.teams = teams;
		this.account = account;
	}

	public String getDataId() {
		return dataId;
	}

	public void setDataId(String dataId) {
		this.dataId = dataId;
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

	public boolean isLoginAllowed() {
		return isLoginAllowed;
	}

	public void setLoginAllowed(boolean isLoginAllowed) {
		this.isLoginAllowed = isLoginAllowed;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Integer getLoginAttempts() {
		return loginAttempts;
	}

	public void setLoginAttempts(Integer loginAttempts) {
		this.loginAttempts = loginAttempts;
	}

	public boolean isDeleted() {
		return deleted;
	}

	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}

	public String getDateCreated() {
		return dateCreated;
	}

	public void setDateCreated(String dateCreated) {
		this.dateCreated = dateCreated;
	}

	public String getDateUpdated() {
		return dateUpdated;
	}

	public void setDateUpdated(String dateUpdated) {
		this.dateUpdated = dateUpdated;
	}

	public boolean isEmailVerified() {
		return emailVerified;
	}

	public void setEmailVerified(boolean emailVerified) {
		this.emailVerified = emailVerified;
	}

	public Phone getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(Phone phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public boolean isDisabled() {
		return disabled;
	}

	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}

	public String getNotificationSound() {
		return notificationSound;
	}

	public void setNotificationSound(String notificationSound) {
		this.notificationSound = notificationSound;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getDefaultContactMethod() {
		return defaultContactMethod;
	}

	public void setDefaultContactMethod(String defaultContactMethod) {
		this.defaultContactMethod = defaultContactMethod;
	}

	public String getLastSeen() {
		return lastSeen;
	}

	public void setLastSeen(String lastSeen) {
		this.lastSeen = lastSeen;
	}

	public String getLastUpdated() {
		return lastUpdated;
	}

	public void setLastUpdated(String lastUpdated) {
		this.lastUpdated = lastUpdated;
	}

	public boolean isInviteAccepted() {
		return inviteAccepted;
	}

	public void setInviteAccepted(boolean inviteAccepted) {
		this.inviteAccepted = inviteAccepted;
	}

	public boolean isSubscriptionOnMarketingEmail() {
		return subscriptionOnMarketingEmail;
	}

	public void setSubscriptionOnMarketingEmail(boolean subscriptionOnMarketingEmail) {
		this.subscriptionOnMarketingEmail = subscriptionOnMarketingEmail;
	}

	public List<String> getTeams() {
		return teams;
	}

	public void setTeams(List<String> teams) {
		this.teams = teams;
	}

	public String getAccount() {
		return account;
	}

	public void setAccount(String account) {
		this.account = account;
	}
}
