package com.ngdesk.companies;

import java.sql.Timestamp;

import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Pattern.Flag;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ngdesk.annotations.Timezone;
import com.ngdesk.annotations.UniqueCompany;
import com.ngdesk.users.Phone;

public class Company {

	@JsonProperty("COMPANY_NAME")
	@NotNull(message = "COMPANY_NAME_NOT_NULL")
	@Size(min = 1, max = 63, message = "COMPANY_NAME_NOT_EMPTY")
	@Pattern(regexp = "([A-Za-z0-9\\-]+)", message = "INVALID_COMPANY_NAME")
	private String companyName;

	@JsonProperty("COMPANY_SUBDOMAIN")
	@NotNull(message = "COMPANY_SUBDOMAIN_NOT_NULL")
	@Size(min = 1, max = 63, message = "COMPANY_SUBDOMAIN_NOT_EMPTY")
	@Pattern(regexp = "([A-Za-z0-9\\-]+)", message = "INVALID_SUBDOMAIN")
	@UniqueCompany(message = "COMPANY_NOT_UNIQUE")
	private String companySubdomain;

	@JsonProperty("EMAIL_ADDRESS")
	@NotNull(message = "EMAIL_ADDRESS_NOT_NULL")
	@Size(min = 1, max = 255, message = "EMAIL_ADDRESS_NOT_EMPTY")
	@Email(flags = Flag.CASE_INSENSITIVE, message = "EMAIL_INVALID")
	@Pattern(regexp = "^(.+)@(.+)\\.(.+)$", message = "EMAIL_INVALID")
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

	@JsonProperty("HIDDEN_FIELD")
	@Size(min = 0, max = 0, message = "HIDDEN_FIELD_NULL")
	private String hiddenField;

	@JsonProperty("PASSWORD")
	@NotNull(message = "PASSWORD_NOT_NULL")
	@Pattern(regexp = "^(?=.*?[A-Z])(?=.*?[#?!@$%^&*-]).{8,}$", message = "PASSWORD_INVALID")
	private String password;

	@JsonProperty("LANGUAGE")
	@NotNull(message = "LANGUAGE_NOT_NULL")
	@Size(max = 2, message = "LANGUAGE_MUST_BE_2_CHAR")
	@Pattern(regexp = "^[a-z]{2}$", message = "LANGUAGE_MUST_BE_CHAR")
	private String language;

	@JsonProperty("PHONE")
	@NotNull(message = "PHONE_NOT_NULL")
	@Valid
	private Phone phone;

	@JsonProperty("TIMEZONE")
	@NotNull(message = "TIMEZONE_NOT_NULL")
	@Timezone(message = "TIMEZONE_INVALID")
	private String timezone;

	@JsonProperty("SOCIAL_SIGN_IN")
	private SocialSignIn socialSignIn;

	@JsonProperty("DATE_CREATED")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
	private Timestamp dateCreated;

	@JsonProperty("DATE_UPDATED")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
	private Timestamp dateUpdated;

	@JsonProperty("PRICING_TIER")
	@NotEmpty(message = "PRICING_REQUIRED")
	@Pattern(regexp = "free|team|professional", message = "NOT_VALID_PRICING_TIER")
	private String pricing;

	@JsonProperty("SOURCE")
	private String source;

	@JsonProperty("LOCALE")
	@Pattern(regexp = "^[a-zA-Z-]{2,12}$", message = "LOCALE_MUST_BE_CHAR")
	private String locale;

	@JsonProperty("GETTING_STARTED")
	private boolean gettingStarted;

	@JsonProperty("FIRST_SIGNIN")
	private boolean firstSignin;

	@JsonProperty("REFERED_BY")
	private Referral referral;

	@JsonProperty("LANDING_PAGE")
	@Pattern(regexp = "landing-page|sam-landing-page|signup-new|signup", message = "INVALID_LANDING_PAGE")
	private String landingPage;

	public Company() {

	}

	public Company(
			@NotNull(message = "COMPANY_NAME_NOT_NULL") @Size(min = 1, max = 63, message = "COMPANY_NAME_NOT_EMPTY") @Pattern(regexp = "([A-Za-z0-9\\-]+)", message = "INVALID_COMPANY_NAME") String companyName,
			@NotNull(message = "COMPANY_SUBDOMAIN_NOT_NULL") @Size(min = 1, max = 63, message = "COMPANY_SUBDOMAIN_NOT_EMPTY") @Pattern(regexp = "([A-Za-z0-9\\-]+)", message = "INVALID_SUBDOMAIN") String companySubdomain,
			@NotNull(message = "EMAIL_ADDRESS_NOT_NULL") @Size(min = 1, max = 255, message = "EMAIL_ADDRESS_NOT_EMPTY") @Email(flags = Flag.CASE_INSENSITIVE, message = "EMAIL_INVALID") @Pattern(regexp = "^(.+)@(.+)\\.(.+)$", message = "EMAIL_INVALID") String emailAddress,
			@NotNull(message = "FIRST_NAME_NOT_NULL") @Size(min = 1, message = "FIRST_NAME_NOT_EMPTY") @Pattern(regexp = "^[A-Za-z0-9\\-_ ]*$", message = "FIRST_NAME_INVALID") String firstName,
			@NotNull(message = "LAST_NAME_NOT_NULL") @Size(min = 1, message = "LAST_NAME_NOT_EMPTY") @Pattern(regexp = "^[A-Za-z0-9\\-_ ]*$", message = "LAST_NAME_INVALID") String lastName,
			@Size(min = 0, max = 0, message = "HIDDEN_FIELD_NULL") String hiddenField,
			@NotNull(message = "PASSWORD_NOT_NULL") @Pattern(regexp = "^(?=.*?[A-Z])(?=.*?[#?!@$%^&*-]).{8,}$", message = "PASSWORD_INVALID") String password,
			@NotNull(message = "LANGUAGE_NOT_NULL") @Size(max = 2, message = "LANGUAGE_MUST_BE_2_CHAR") @Pattern(regexp = "^[a-z]{2}$", message = "LANGUAGE_MUST_BE_CHAR") String language,
			@NotNull(message = "PHONE_NOT_NULL") @Valid Phone phone,
			@NotNull(message = "TIMEZONE_NOT_NULL") String timezone, SocialSignIn socialSignIn, Timestamp dateCreated,
			Timestamp dateUpdated,
			@NotEmpty(message = "PRICING_REQUIRED") @Pattern(regexp = "free|team|professional", message = "NOT_VALID_PRICING_TIER") String pricing,
			String source, @Pattern(regexp = "^[a-zA-Z-]{2,12}$", message = "LOCALE_MUST_BE_CHAR") String locale,
			boolean gettingStarted, boolean firstSignin, Referral referral, String landingPage) {
		super();
		this.companyName = companyName;
		this.companySubdomain = companySubdomain;
		this.emailAddress = emailAddress;
		this.firstName = firstName;
		this.lastName = lastName;
		this.hiddenField = hiddenField;
		this.password = password;
		this.language = language;
		this.phone = phone;
		this.timezone = timezone;
		this.socialSignIn = socialSignIn;
		this.dateCreated = dateCreated;
		this.dateUpdated = dateUpdated;
		this.pricing = pricing;
		this.source = source;
		this.locale = locale;
		this.gettingStarted = gettingStarted;
		this.firstSignin = firstSignin;
		this.referral = referral;
		this.landingPage = landingPage;
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

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getHiddenField() {
		return hiddenField;
	}

	public void setHiddenField(String hiddenField) {
		this.hiddenField = hiddenField;
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

	public SocialSignIn getSocialSignIn() {
		return socialSignIn;
	}

	public void setSocialSignIn(SocialSignIn socialSignIn) {
		this.socialSignIn = socialSignIn;
	}

	public Timestamp getDateCreated() {
		return dateCreated;
	}

	public void setDateCreated(Timestamp dateCreated) {
		this.dateCreated = dateCreated;
	}

	public Timestamp getDateUpdated() {
		return dateUpdated;
	}

	public void setDateUpdated(Timestamp dateUpdated) {
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

	public boolean isFirstSignin() {
		return firstSignin;
	}

	public void setFirstSignin(boolean firstSignin) {
		this.firstSignin = firstSignin;
	}

	public Referral getReferral() {
		return referral;
	}

	public void setReferral(Referral referral) {
		this.referral = referral;
	}

	public String getLandingPage() {
		return landingPage;
	}

	public void setLandingPage(String landingPage) {
		this.landingPage = landingPage;
	}

}
