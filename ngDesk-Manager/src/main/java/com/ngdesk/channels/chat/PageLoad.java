package com.ngdesk.channels.chat;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(Include.NON_NULL)
public class PageLoad {

	@JsonProperty("COMPANY_UUID")
	private String companyUUID;

	@JsonProperty("IP_ADDRESS")
	private String ipAddress;

	@JsonProperty("USER_AGENT")
	private String userAgent;

	@JsonProperty("WIDGET_ID")
	private String widgetId;

	@JsonProperty("BROWSER")
	private String browser;

	@JsonProperty("FULL_URL")
	private String fullUrl;

	@JsonProperty("LOCATION")
	private String location;

	@JsonProperty("PAGE_TITLE")
	private String pageTitle;

	@JsonProperty("PLATFORM")
	private String platform;

	@JsonProperty("DEVICE")
	private String device;

	@JsonProperty("HOSTNAME")
	private String hostName;

	@JsonProperty("SESSION_UUID")
	private String sessionUUID;
	
	@JsonProperty("FIRST_NAME")
	private String firstName;

	@JsonProperty("LAST_NAME")
	private String lastName;

	@JsonProperty("EMAIL_ADDRESS")
	private String emailAddress;
	
	@JsonProperty("REFERRER")
	private String referrer;
	
	@JsonProperty("START_CHAT")
	private boolean started;
	
	@JsonProperty("COUNTRY")
	private String country;

	public PageLoad() {

	}


	public PageLoad(String companyUUID, String ipAddress, String userAgent, String widgetId, String browser,
			String fullUrl, String location, String pageTitle, String platform, String device, String hostName,
			String sessionUUID, String firstName, String lastName, String emailAddress, String referrer,
			boolean started, String country) {
		super();
		this.companyUUID = companyUUID;
		this.ipAddress = ipAddress;
		this.userAgent = userAgent;
		this.widgetId = widgetId;
		this.browser = browser;
		this.fullUrl = fullUrl;
		this.location = location;
		this.pageTitle = pageTitle;
		this.platform = platform;
		this.device = device;
		this.hostName = hostName;
		this.sessionUUID = sessionUUID;
		this.firstName = firstName;
		this.lastName = lastName;
		this.emailAddress = emailAddress;
		this.referrer = referrer;
		this.started = started;
		this.country = country;
	}


	public String getCompanyUUID() {
		return companyUUID;
	}


	public void setCompanyUUID(String companyUUID) {
		this.companyUUID = companyUUID;
	}


	public String getIpAddress() {
		return ipAddress;
	}


	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}


	public String getUserAgent() {
		return userAgent;
	}


	public void setUserAgent(String userAgent) {
		this.userAgent = userAgent;
	}


	public String getWidgetId() {
		return widgetId;
	}


	public void setWidgetId(String widgetId) {
		this.widgetId = widgetId;
	}


	public String getBrowser() {
		return browser;
	}


	public void setBrowser(String browser) {
		this.browser = browser;
	}


	public String getFullUrl() {
		return fullUrl;
	}


	public void setFullUrl(String fullUrl) {
		this.fullUrl = fullUrl;
	}


	public String getLocation() {
		return location;
	}


	public void setLocation(String location) {
		this.location = location;
	}


	public String getPageTitle() {
		return pageTitle;
	}


	public void setPageTitle(String pageTitle) {
		this.pageTitle = pageTitle;
	}


	public String getPlatform() {
		return platform;
	}


	public void setPlatform(String platform) {
		this.platform = platform;
	}


	public String getDevice() {
		return device;
	}


	public void setDevice(String device) {
		this.device = device;
	}


	public String getHostName() {
		return hostName;
	}


	public void setHostName(String hostName) {
		this.hostName = hostName;
	}


	public String getSessionUUID() {
		return sessionUUID;
	}


	public void setSessionUUID(String sessionUUID) {
		this.sessionUUID = sessionUUID;
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


	public String getReferrer() {
		return referrer;
	}


	public void setReferrer(String referrer) {
		this.referrer = referrer;
	}


	public boolean isStarted() {
		return started;
	}


	public void setStarted(boolean started) {
		this.started = started;
	}


	public String getCountry() {
		return country;
	}


	public void setCountry(String country) {
		this.country = country;
	}

}