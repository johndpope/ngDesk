package com.ngdesk.websocket.channels.chat.dao;

import java.util.List;

import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

public class PageLoad {

	@JsonProperty("COMPANY_UUID")
	@Field("COMPANY_UUID")
	private String companyUUID;

	@JsonProperty("IP_ADDRESS")
	@Field("IP_ADDRESS")
	private String ipAddress;

	@JsonProperty("USER_AGENT")
	@Field("USER_AGENT")
	private String userAgent;

	@JsonProperty("WIDGET_ID")
	@Field("WIDGET_ID")
	private String widgetId;

	@JsonProperty("BROWSER")
	@Field("BROWSER")
	private String browser;

	@JsonProperty("FULL_URL")
	@Field("FULL_URL")
	private String fullUrl;

	@JsonProperty("LOCATION")
	@Field("LOCATION")
	private String location;

	@JsonProperty("PAGE_TITLE")
	@Field("PAGE_TITLE")
	private String pageTitle;

	@JsonProperty("PLATFORM")
	@Field("PLATFORM")
	private String platform;

	@JsonProperty("DEVICE_TYPE")
	@Field("DEVICE_TYPE")
	private String deviceType;

	@JsonProperty("HOSTNAME")
	@Field("HOSTNAME")
	private String hostName;

	@JsonProperty("SESSION_UUID")
	@Field("SESSION_UUID")
	private String sessionUUID;

	@JsonProperty("FIRST_NAME")
	@Field("FIRST_NAME")
	private String firstName;

	@JsonProperty("LAST_NAME")
	@Field("LAST_NAME")
	private String lastName;

	@JsonProperty("EMAIL_ADDRESS")
	@Field("EMAIL_ADDRESS")
	private String emailAddress;

	@JsonProperty("REFERRER")
	@Field("REFERRER")
	private String referrer;

	@JsonProperty("START_CHAT")
	@Field("START_CHAT")
	private boolean started;

	@JsonProperty("CHANNEL")
	@Field("CHANNEL")
	private String channel;

	@JsonProperty("COUNTRY")
	@Field("COUNTRY")
	private String country;

	@JsonProperty("STATUS")
	@Field("STATUS")
	private String status;

	public PageLoad() {

	}

	public PageLoad(String companyUUID, String ipAddress, String userAgent, String widgetId, String browser,
			String fullUrl, String location, String pageTitle, String platform, String deviceType, String hostName,
			String sessionUUID, String firstName, String lastName, String emailAddress, String referrer,
			boolean started, String channel, String country, String status) {
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
		this.deviceType = deviceType;
		this.hostName = hostName;
		this.sessionUUID = sessionUUID;
		this.firstName = firstName;
		this.lastName = lastName;
		this.emailAddress = emailAddress;
		this.referrer = referrer;
		this.started = started;
		this.channel = channel;
		this.country = country;
		this.status = status;
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

	public String getDeviceType() {
		return deviceType;
	}

	public void setDeviceType(String deviceType) {
		this.deviceType = deviceType;
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

	public String getChannel() {
		return channel;
	}

	public void setChannel(String channel) {
		this.channel = channel;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

}