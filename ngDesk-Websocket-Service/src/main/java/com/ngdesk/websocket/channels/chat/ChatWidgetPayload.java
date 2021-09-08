package com.ngdesk.websocket.channels.chat;

import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ChatWidgetPayload {

	@JsonProperty("IP_ADDRESS")
	@Field("IP_ADDRESS")
	private String ipAddress;

	@JsonProperty("USER_AGENT")
	@Field("USER_AGENT")
	private String userAgent;
	
	@JsonProperty("BROWSER")
	@Field("BROWSER")
	private String browser;
	
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

	@JsonProperty("REFERRER")
	@Field("REFERRER")
	private String referrer;
	
	@JsonProperty("COUNTRY")
	@Field("COUNTRY")
	private String country;

	@JsonProperty("COMPANY_SUBDOMAIN")
	@Field("COMPANY_SUBDOMAIN")
	private String companySubdomain;

	@JsonProperty("MESSAGE_TYPE")
	@Field("MESSAGE_TYPE")
	private String messageType;
	
	public ChatWidgetPayload() {
			
	}
		
	public ChatWidgetPayload(String ipAddress, String userAgent, String browser, String location, String pageTitle,
			String platform, String deviceType, String hostName, String sessionUUID, String referrer, String country,
			String companySubdomain, String messageType) {
		super();
		this.ipAddress = ipAddress;
		this.userAgent = userAgent;
		this.browser = browser;
		this.location = location;
		this.pageTitle = pageTitle;
		this.platform = platform;
		this.deviceType = deviceType;
		this.hostName = hostName;
		this.sessionUUID = sessionUUID;
		this.referrer = referrer;
		this.country = country;
		this.companySubdomain = companySubdomain;
		this.messageType = messageType;
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

	public String getBrowser() {
		return browser;
	}

	public void setBrowser(String browser) {
		this.browser = browser;
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

	public String getReferrer() {
		return referrer;
	}

	public void setReferrer(String referrer) {
		this.referrer = referrer;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getCompanySubdomain() {
		return companySubdomain;
	}

	public void setCompanySubdomain(String companySubdomain) {
		this.companySubdomain = companySubdomain;
	}

	public String getMessageType() {
		return messageType;
	}

	public void setMessageType(String messageType) {
		this.messageType = messageType;
	}
	
	
	

	

	
}
