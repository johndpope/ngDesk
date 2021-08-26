package com.ngdesk.flowmanager;

import java.util.List;
import java.util.Map;

import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;

public class InputMessage {

	@JsonProperty("FIRST_NAME")
	@Field("FIRST_NAME")
	private String firstName;

	@JsonProperty("LAST_NAME")
	@Field("LAST_NAME")
	private String lastName;

	@JsonProperty("EMAIL_ADDRESS")
	@Field("EMAIL_ADDRESS")
	private String emailAddress;

	@JsonProperty("USER_ID")
	@Field("USER_ID")
	private String userId;

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

	@JsonProperty("LOCATION")
	@Field("LOCATION")
	private String location;

	@JsonProperty("PAGE_TITLE")
	@Field("PAGE_TITLE")
	private String pageTitle;

	@JsonProperty("PLATFORM")
	@Field("PLATFORM")
	private String platform;

	@JsonProperty("DEVICE")
	@Field("DEVICE")
	private String device;

	@JsonProperty("HOSTNAME")
	@Field("HOSTNAME")
	private String hostName;

	@JsonProperty("FULL_URL")
	@Field("FULL_URL")
	private String fullUrl;

	@JsonProperty("SESSION_UUID")
	@Field("SESSION_UUID")
	private String sessionUUID;

	@JsonProperty("SESSION_PASSWORD")
	@Field("SESSION_PASSWORD")
	private String sessionPassword;

	@JsonProperty("USER_UUID")
	@Field("USER_UUID")
	private String userUUID;

	@JsonProperty("MESSAGE_TYPE")
	@Field("MESSAGE_TYPE")
	private String messageType;

	@JsonProperty("TYPE")
	@Field("TYPE")
	private String type;

	@JsonProperty("CHANNEL_NAME")
	@Field("CHANNEL_NAME")
	private String channelName;

	@JsonProperty("TO")
	@Field("TO")
	private String to;

	@JsonProperty("FROM")
	@Field("FROM")
	private String from;

	@JsonProperty("SUBJECT")
	@Field("SUBJECT")
	private String subject;

	@JsonProperty("BODY")
	@Field("BODY")
	private String body;

	@JsonProperty("CC_EMAILS")
	@Field("CC_EMAILS")
	private List<String> cc;

	@JsonProperty("ATTACHMENTS")
	@Field("ATTACHMENTS")
	private List<Attachment> attachments;

	@JsonProperty("DATA_ID")
	@Field("DATA_ID")
	private String dataId;

	@JsonProperty("SENDER")
	@Field("SENDER")
	private Map<String, Object> senderDetails;

	@JsonProperty("MODULE")
	@Field("MODULE")
	private String moduleId;

	@JsonProperty("REFERRER")
	@Field("REFERRER")
	private String referrer;

	public InputMessage() {

	}

	public InputMessage(String firstName, String lastName, String emailAddress, String userId, String companyUUID,
			String ipAddress, String userAgent, String widgetId, String browser, String location, String pageTitle,
			String platform, String device, String hostName, String fullUrl, String sessionUUID, String sessionPassword,
			String userUUID, String messageType, String type, String channelName, String to, String from,
			String subject, String body, List<String> cc, List<Attachment> attachments, String dataId,
			Map<String, Object> senderDetails, String moduleId, String referrer) {
		super();
		this.firstName = firstName;
		this.lastName = lastName;
		this.emailAddress = emailAddress;
		this.userId = userId;
		this.companyUUID = companyUUID;
		this.ipAddress = ipAddress;
		this.userAgent = userAgent;
		this.widgetId = widgetId;
		this.browser = browser;
		this.location = location;
		this.pageTitle = pageTitle;
		this.platform = platform;
		this.device = device;
		this.hostName = hostName;
		this.fullUrl = fullUrl;
		this.sessionUUID = sessionUUID;
		this.sessionPassword = sessionPassword;
		this.userUUID = userUUID;
		this.messageType = messageType;
		this.type = type;
		this.channelName = channelName;
		this.to = to;
		this.from = from;
		this.subject = subject;
		this.body = body;
		this.cc = cc;
		this.attachments = attachments;
		this.dataId = dataId;
		this.senderDetails = senderDetails;
		this.moduleId = moduleId;
		this.referrer = referrer;
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

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
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

	public String getFullUrl() {
		return fullUrl;
	}

	public void setFullUrl(String fullUrl) {
		this.fullUrl = fullUrl;
	}

	public String getSessionUUID() {
		return sessionUUID;
	}

	public void setSessionUUID(String sessionUUID) {
		this.sessionUUID = sessionUUID;
	}

	public String getSessionPassword() {
		return sessionPassword;
	}

	public void setSessionPassword(String sessionPassword) {
		this.sessionPassword = sessionPassword;
	}

	public String getUserUUID() {
		return userUUID;
	}

	public void setUserUUID(String userUUID) {
		this.userUUID = userUUID;
	}

	public String getMessageType() {
		return messageType;
	}

	public void setMessageType(String messageType) {
		this.messageType = messageType;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getChannelName() {
		return channelName;
	}

	public void setChannelName(String channelName) {
		this.channelName = channelName;
	}

	public String getTo() {
		return to;
	}

	public void setTo(String to) {
		this.to = to;
	}

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public List<String> getCc() {
		return cc;
	}

	public void setCc(List<String> cc) {
		this.cc = cc;
	}

	public List<Attachment> getAttachments() {
		return attachments;
	}

	public void setAttachments(List<Attachment> attachments) {
		this.attachments = attachments;
	}

	public String getDataId() {
		return dataId;
	}

	public void setDataId(String dataId) {
		this.dataId = dataId;
	}

	public Map<String, Object> getSenderDetails() {
		return senderDetails;
	}

	public void setSenderDetails(Map<String, Object> senderDetails) {
		this.senderDetails = senderDetails;
	}

	public String getModuleId() {
		return moduleId;
	}

	public void setModuleId(String moduleId) {
		this.moduleId = moduleId;
	}

	public String getReferrer() {
		return referrer;
	}

	public void setReferrer(String referrer) {
		this.referrer = referrer;
	}

}
