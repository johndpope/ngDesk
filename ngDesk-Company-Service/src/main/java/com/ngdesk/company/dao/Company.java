package com.ngdesk.company.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Pattern.Flag;
import javax.validation.constraints.Size;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ngdesk.commons.annotations.CustomNotEmpty;
import com.ngdesk.commons.annotations.CustomNotNull;
import com.ngdesk.commons.annotations.CustomTimeZoneValidation;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.AccessMode;

public class Company {

	@Schema(required = false, accessMode = AccessMode.READ_ONLY, description = "Autogenerated Id")
	@JsonProperty("COMPANY_ID")
	@Id
	private String companyId;

	@Schema(required = true, description = "Name of the company", example = "company-name")
	@JsonProperty("COMPANY_NAME")
	@CustomNotEmpty(message = "DAO_VARIABLE_REQUIRED", values = { "COMPANY_NAME" })
	@Size(min = 1, max = 63, message = "COMPANY_NAME_SIZE_INVALID")
	@Field("COMPANY_NAME")
	private String companyName;

	@Schema(required = true, description = "Subdomain of the company", example = "support-test")
	@JsonProperty("COMPANY_SUBDOMAIN")
	@CustomNotEmpty(message = "DAO_VARIABLE_REQUIRED", values = { "COMPANY_SUBDOMAIN" })
	@Size(min = 1, max = 63, message = "COMPANY_SUBDOMAIN_SIZE_INVALID")
	@Pattern(regexp = "([A-Za-z0-9\\-]+)", message = "INVALID_COMPANY_SUBDOMAIN")
	// TODO:ADD CUSTOM ANNOTATION UNIQUE SUBDOMAIN
	@Field("COMPANY_SUBDOMAIN")
	private String companySubdomain;

	@Schema(required = true, description = "Email address of the user", example = "user@domain.com")
	@JsonProperty("EMAIL_ADDRESS")
	@CustomNotEmpty(message = "DAO_VARIABLE_REQUIRED", values = { "EMAIL_ADDRESS" })
	@Size(min = 1, max = 255, message = "EMAIL_ADDRESS_SIZE_INVALID")
	@Email(flags = Flag.CASE_INSENSITIVE, message = "EMAIL_ADDRESS_INVALID")
	@Pattern(regexp = "^(.+)@(.+)\\.(.+)$", message = "EMAIL_ADDRESS_INVALID")
	@Transient
	private String emailAddress;

	@Schema(required = true, description = "First name of the user")
	@JsonProperty("FIRST_NAME")
	@CustomNotEmpty(message = "DAO_VARIABLE_REQUIRED", values = { "FIRST_NAME" })
	@Pattern(regexp = "^[A-Za-z0-9\\-_ ]*$", message = "FIRST_NAME_INVALID")
	@Transient
	private String firstName;

	@JsonProperty("HIDDEN_FIELD")
	private String hiddenField;;

	@Schema(required = true, description = "Last name of the user")
	@JsonProperty("LAST_NAME")
	@CustomNotEmpty(message = "DAO_VARIABLE_REQUIRED", values = { "LAST_NAME" })
	@Pattern(regexp = "^[A-Za-z0-9\\-_ ]*$", message = "LAST_NAME_INVALID")
	@Transient
	private String lastName;

	@Schema(required = true, description = "Password for the user (minimum 8 chars)")
	@JsonProperty("PASSWORD")
	@CustomNotEmpty(message = "DAO_VARIABLE_REQUIRED", values = { "PASSWORD" })
	@Pattern(regexp = "^(?=.*?[A-Z])(?=.*?[#?!@$%^&*-]).{8,}$", message = "PASSWORD_INVALID")
	@Transient
	private String password;

	@Schema(required = true, description = "Language of the company", example = "English")
	@JsonProperty("LANGUAGE")
	@CustomNotEmpty(message = "DAO_VARIABLE_REQUIRED", values = { "LANGUAGE" })
	@Pattern(regexp = "^[A-Za-z]*$", message = "LANGUAGE_MUST_BE_CHAR")
	@Field("LANGUAGE")
	private String language = "English";

	@Schema(required = true, description = "Phone number of the user")
	@JsonProperty("PHONE")
	@CustomNotNull(message = "NOT_NULL", values = { "PHONE" })
	@Valid
	@Transient
	private Phone phone;

	@Schema(required = true, description = "Timezone of the company", example = "America/Chicago")
	@JsonProperty("TIMEZONE")
	@CustomNotEmpty(message = "DAO_VARIABLE_REQUIRED", values = { "COMPANY_TIMEZONE" })
	@CustomTimeZoneValidation(message = "INVALID_TIMEZONE", values = { "COMPANY_TIMEZONE" })
	@Field("TIMEZONE")
	private String timezone;

	@Schema(required = false, description = "Date of company creation")
	@JsonProperty("DATE_CREATED")
	@Field("DATE_CREATED")
	private Date dateCreated = new Date();

	@Schema(required = false, hidden = true)
	@JsonProperty("DATE_UPDATED")
	@Field("DATE_UPDATED")
	private Date dateUpdated = new Date();

	@Schema(required = true, description = "Pricing of the company", example = "support-test")
	@JsonProperty("PRICING_TIER")
	@CustomNotEmpty(message = "DAO_VARIABLE_REQUIRED", values = { "PRICING_TIER" })
	@Pattern(regexp = "free|team|professional", message = "NOT_VALID_PRICING_TIER")
	@Field("PRICING")
	private String pricing = "free";

	@Schema(required = true, description = "Source of the company")
	@JsonProperty("SOURCE")
	@Field("SOURCE")
	private String source;

	@Schema(required = true, description = "Locale of the company", example = "en-US")
	@JsonProperty("LOCALE")
	@Pattern(regexp = "^[a-zA-Z-]{2,12}$", message = "LOCALE_MUST_BE_CHAR")
	@Field("LOCALE")
	private String locale = "en-US";

	@Schema(required = false, hidden = true, description = "Getting started")
	@JsonProperty("GETTING_STARTED")
	@Field("GETTING_STARTED")
	private boolean gettingStarted = false;

	@Schema(required = false, hidden = true, description = "First signin")
	@JsonProperty("FIRST_SIGNIN")
	@Field("FIRST_SIGNIN")
	private boolean firstSiginIn = false;

	@Schema(required = false, hidden = true, description = "Unique uuid for company")
	@JsonProperty("COMPANY_UUID")
	@Field("COMPANY_UUID")
	private String companyUuid = UUID.randomUUID().toString();

	@Schema(required = true, description = "Plugins that has to be added to the company")
	@JsonProperty("PLUGINS")
	@Field("PLUGINS")
	@CustomNotEmpty(message = "DAO_VARIABLE_REQUIRED", values = { "PLUGINS" })
	@Size(min = 1, message = "PLUGINS_REQUIRED")
	private List<String> plugins;

	@Schema(required = false, hidden = true, description = "Social signin options")
	@JsonProperty("SOCIAL_SIGN_IN")
	@Field("SOCIAL_SIGN_IN")
	private SocialSignIn socialSignIn = new SocialSignIn();

	@Schema(required = false, hidden = true, description = "Wildcard emails")
	@JsonProperty("ALLOW_WILDCARD_EMAILS")
	@Field("ALLOW_WILDCARD_EMAILS")
	private boolean wildcardEmails = false;

	@Schema(required = false, hidden = true)
	@JsonProperty("INDUSTRY")
	@Field("INDUSTRY")
	private String industry;

	@Schema(required = false, hidden = true)
	@JsonProperty("DEPARTMENT")
	@Field("DEPARTMENT")
	private String department;

	@Schema(required = false, hidden = true)
	@JsonProperty("SIZE")
	@Field("SIZE")
	private String size;

	@Schema(required = false, description = "Company version")
	@JsonProperty("VERSION")
	@Field("VERSION")
	private String version;

	@Schema(required = false, hidden = true)
	@JsonProperty("TRACKING")
	@Field("TRACKING")
	private Tracking tracking;

	@Schema(required = false, hidden = true)
	@JsonProperty("ENABLE_DOCS")
	@Field("ENABLE_DOCS")
	private boolean enableDocs = true;

	// TODO: REMOVE IF NOT REQURIED
	@JsonProperty("MAX_CHATS_PER_AGENT")
	@Field("MAX_CHATS_PER_AGENT")
	private int maxChatPerAgent = 5;

	@Schema(required = false, hidden = true)
	@JsonProperty("THEMES")
	@Field("THEMES")
	private Themes theme = new Themes();

	@Schema(required = false, description = "Company referal information")
	@JsonProperty("REFERED_BY")
	@Field("REFERED_BY")
	private Referral referal;

	// TODO: CHECK AND REMOVE IF NOT REQURIED
	@JsonProperty("LANDING_PAGE")
	@Field("LANDING_PAGE")
	private String landingPage;

	@Schema(required = false, hidden = true)
	@JsonProperty("SIGNUP_MESSAGE")
	@Field("SIGNUP_MESSAGE")
	private SignUpMessage signupMessage = new SignUpMessage();

	@Schema(required = false, hidden = true)
	@JsonProperty("INVITE_MESSAGE")
	@Field("INVITE_MESSAGE")
	private InviteMessage inviteMessage;

	@Schema(required = false, hidden = true)
	@JsonProperty("FORGOT_PASSWORD_MESSAGE")
	@Field("FORGOT_PASSWORD_MESSAGE")
	private ForgotPasswordMessage forgotPasswordMessage;

	@Schema(required = false, description = "Domain for on-premise version")
	@JsonProperty("DOMAIN")
	@Field("DOMAIN")
	private String domain;

	@Schema(required = false, description = "Number of users for on-premise version")
	@JsonProperty("NUMBER_OF_USERS")
	@Field("NUMBER_OF_USERS")
	private int numberOfUsers;

	@Schema(required = false, description = "account level access setting")
	@JsonProperty("ACCOUNT_LEVEL_ACCESS")
	@Field("ACCOUNT_LEVEL_ACCESS")
	private boolean accountLevelAccess;

	@Schema(required = false, description = "Roles with chat access")
	@JsonProperty("ROLES_WITH_CHAT")
	@Field("ROLES_WITH_CHAT")
	private ArrayList<String> rolesWithChat;

	@Schema(required = false, description = "usage type of ngdesk application")
	@JsonProperty("USAGE_TYPE")
	@Field("USAGE_TYPE")
	private UsageType usageType;

	public Company() {
		super();
	}

	public Company(String companyId, @Size(min = 1, max = 63, message = "COMPANY_NAME_SIZE_INVALID") String companyName,
			@Size(min = 1, max = 63, message = "COMPANY_SUBDOMAIN_SIZE_INVALID") @Pattern(regexp = "([A-Za-z0-9\\-]+)", message = "INVALID_COMPANY_SUBDOMAIN") String companySubdomain,
			@Size(min = 1, max = 255, message = "EMAIL_ADDRESS_SIZE_INVALID") @Email(flags = Flag.CASE_INSENSITIVE, message = "EMAIL_ADDRESS_INVALID") @Pattern(regexp = "^(.+)@(.+)\\.(.+)$", message = "EMAIL_ADDRESS_INVALID") String emailAddress,
			@Pattern(regexp = "^[A-Za-z0-9\\-_ ]*$", message = "FIRST_NAME_INVALID") String firstName,
			String hiddenField, @Pattern(regexp = "^[A-Za-z0-9\\-_ ]*$", message = "LAST_NAME_INVALID") String lastName,
			@Pattern(regexp = "^(?=.*?[A-Z])(?=.*?[#?!@$%^&*-]).{8,}$", message = "PASSWORD_INVALID") String password,
			@Pattern(regexp = "^[A-Za-z]*$", message = "LANGUAGE_MUST_BE_CHAR") String language, @Valid Phone phone,
			String timezone, Date dateCreated, Date dateUpdated,
			@Pattern(regexp = "free|team|professional", message = "NOT_VALID_PRICING_TIER") String pricing,
			String source, @Pattern(regexp = "^[a-zA-Z-]{2,12}$", message = "LOCALE_MUST_BE_CHAR") String locale,
			boolean gettingStarted, boolean firstSiginIn, String companyUuid,
			@Size(min = 1, message = "PLUGINS_REQUIRED") List<String> plugins, SocialSignIn socialSignIn,
			boolean wildcardEmails, String industry, String department, String size, String version, Tracking tracking,
			boolean enableDocs, int maxChatPerAgent, Themes theme, Referral referal, String landingPage,
			SignUpMessage signupMessage, InviteMessage inviteMessage, ForgotPasswordMessage forgotPasswordMessage,
			String domain, int numberOfUsers, boolean accountLevelAccess, ArrayList<String> rolesWithChat,
			UsageType usageType) {
		super();
		this.companyId = companyId;
		this.companyName = companyName;
		this.companySubdomain = companySubdomain;
		this.emailAddress = emailAddress;
		this.firstName = firstName;
		this.hiddenField = hiddenField;
		this.lastName = lastName;
		this.password = password;
		this.language = language;
		this.phone = phone;
		this.timezone = timezone;
		this.dateCreated = dateCreated;
		this.dateUpdated = dateUpdated;
		this.pricing = pricing;
		this.source = source;
		this.locale = locale;
		this.gettingStarted = gettingStarted;
		this.firstSiginIn = firstSiginIn;
		this.companyUuid = companyUuid;
		this.plugins = plugins;
		this.socialSignIn = socialSignIn;
		this.wildcardEmails = wildcardEmails;
		this.industry = industry;
		this.department = department;
		this.size = size;
		this.version = version;
		this.tracking = tracking;
		this.enableDocs = enableDocs;
		this.maxChatPerAgent = maxChatPerAgent;
		this.theme = theme;
		this.referal = referal;
		this.landingPage = landingPage;
		this.signupMessage = signupMessage;
		this.inviteMessage = inviteMessage;
		this.forgotPasswordMessage = forgotPasswordMessage;
		this.domain = domain;
		this.numberOfUsers = numberOfUsers;
		this.accountLevelAccess = accountLevelAccess;
		this.rolesWithChat = rolesWithChat;
		this.usageType = usageType;
	}

	public String getCompanyId() {
		return companyId;
	}

	public void setCompanyId(String companyId) {
		this.companyId = companyId;
	}

	public String getCompanyName() {
		return companyName;
	}

	public void setCompanyName(String companyName) {
		this.companyName = companyName;
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

	public String getHiddenField() {
		return hiddenField;
	}

	public void setHiddenField(String hiddenField) {
		this.hiddenField = hiddenField;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public Phone getPhone() {
		return phone;
	}

	public void setPhone(Phone phone) {
		this.phone = phone;
	}

	public String getTimezone() {
		return timezone;
	}

	public void setTimezone(String timezone) {
		this.timezone = timezone;
	}

	public Date getDateCreated() {
		return dateCreated;
	}

	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}

	public Date getDateUpdated() {
		return dateUpdated;
	}

	public void setDateUpdated(Date dateUpdated) {
		this.dateUpdated = dateUpdated;
	}

	public String getPricing() {
		return pricing;
	}

	public void setPricing(String pricing) {
		this.pricing = pricing;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getLocale() {
		return locale;
	}

	public void setLocale(String locale) {
		this.locale = locale;
	}

	public boolean isGettingStarted() {
		return gettingStarted;
	}

	public void setGettingStarted(boolean gettingStarted) {
		this.gettingStarted = gettingStarted;
	}

	public boolean isFirstSiginIn() {
		return firstSiginIn;
	}

	public void setFirstSiginIn(boolean firstSiginIn) {
		this.firstSiginIn = firstSiginIn;
	}

	public String getCompanyUuid() {
		return companyUuid;
	}

	public void setCompanyUuid(String companyUuid) {
		this.companyUuid = companyUuid;
	}

	public List<String> getPlugins() {
		return plugins;
	}

	public void setPlugins(List<String> plugins) {
		this.plugins = plugins;
	}

	public SocialSignIn getSocialSignIn() {
		return socialSignIn;
	}

	public void setSocialSignIn(SocialSignIn socialSignIn) {
		this.socialSignIn = socialSignIn;
	}

	public boolean isWildcardEmails() {
		return wildcardEmails;
	}

	public void setWildcardEmails(boolean wildcardEmails) {
		this.wildcardEmails = wildcardEmails;
	}

	public String getIndustry() {
		return industry;
	}

	public void setIndustry(String industry) {
		this.industry = industry;
	}

	public String getDepartment() {
		return department;
	}

	public void setDepartment(String department) {
		this.department = department;
	}

	public String getSize() {
		return size;
	}

	public void setSize(String size) {
		this.size = size;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public Tracking getTracking() {
		return tracking;
	}

	public void setTracking(Tracking tracking) {
		this.tracking = tracking;
	}

	public boolean isEnableDocs() {
		return enableDocs;
	}

	public void setEnableDocs(boolean enableDocs) {
		this.enableDocs = enableDocs;
	}

	public int getMaxChatPerAgent() {
		return maxChatPerAgent;
	}

	public void setMaxChatPerAgent(int maxChatPerAgent) {
		this.maxChatPerAgent = maxChatPerAgent;
	}

	public Themes getTheme() {
		return theme;
	}

	public void setTheme(Themes theme) {
		this.theme = theme;
	}

	public Referral getReferal() {
		return referal;
	}

	public void setReferal(Referral referal) {
		this.referal = referal;
	}

	public String getLandingPage() {
		return landingPage;
	}

	public void setLandingPage(String landingPage) {
		this.landingPage = landingPage;
	}

	public SignUpMessage getSignupMessage() {
		return signupMessage;
	}

	public void setSignupMessage(SignUpMessage signupMessage) {
		this.signupMessage = signupMessage;
	}

	public InviteMessage getInviteMessage() {
		return inviteMessage;
	}

	public void setInviteMessage(InviteMessage inviteMessage) {
		this.inviteMessage = inviteMessage;
	}

	public ForgotPasswordMessage getForgotPasswordMessage() {
		return forgotPasswordMessage;
	}

	public void setForgotPasswordMessage(ForgotPasswordMessage forgotPasswordMessage) {
		this.forgotPasswordMessage = forgotPasswordMessage;
	}

	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

	public int getNumberOfUsers() {
		return numberOfUsers;
	}

	public void setNumberOfUsers(int numberOfUsers) {
		this.numberOfUsers = numberOfUsers;
	}

	public boolean isAccountLevelAccess() {
		return accountLevelAccess;
	}

	public void setAccountLevelAccess(boolean accountLevelAccess) {
		this.accountLevelAccess = accountLevelAccess;
	}

	public ArrayList<String> isRolesWithChat() {
		return rolesWithChat;
	}

	public void setRolesWithChat(ArrayList<String> rolesWithChat) {
		this.rolesWithChat = rolesWithChat;
	}

	public UsageType isUsageType() {
		return usageType;
	}

	public void setUsageType(UsageType usageType) {
		this.usageType = usageType;
	}

}
