package com.ngdesk.tracking;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.Gson;

public class UserEvents {

	@JsonProperty("EVENT_NAME")
	@NotNull(message = "EVENT_NAME_REQUIRED")
	private String eventName;

	@JsonProperty("CUSTOM_PROPERTIES")
	private Object customProperties;

	@JsonProperty("OS")
	@NotNull(message = "OPERATING_SYSTEM_REQUIRED")
	private String operatingSystem;

	@JsonProperty("BROWSER")
	@NotNull(message = "BROWSER_REQUIRED")
	private String browser;

	@JsonProperty("CURRENT_URL")
	@NotNull(message = "CURRENT_URL_REQUIRED")
	private String currentUrl;

	@JsonProperty("BROWSER_VERSION")
	@NotNull(message = "BROWSER_VERSION_REQUIRED")
	private String browserVersion;

	@JsonProperty("SCREEN_HEIGHT")
	@NotNull(message = "SCREEN_HEIGHT_REQUIRED")
	private String screenHeight;

	@JsonProperty("SCREEN_WIDTH")
	@NotNull(message = "SCREEN_WIDTH_REQUIRED")
	private String screenWidth;

	@JsonProperty("DEVICE_ID")
	private String deviceId;

	public UserEvents() {

	}

	public UserEvents(@NotNull(message = "EVENT_NAME_REQUIRED") String eventName, Object customProperties,
			@NotNull(message = "OPERATING_SYSTEM_REQUIRED") String operatingSystem,
			@NotNull(message = "BROWSER_REQUIRED") String browser,
			@NotNull(message = "CURRENT_URL_REQUIRED") String currentUrl,
			@NotNull(message = "BROWSER_VERSION_REQUIRED") String browserVersion,
			@NotNull(message = "SCREEN_HEIGHT_REQUIRED") String screenHeight,
			@NotNull(message = "SCREEN_WIDTH_REQUIRED") String screenWidth, String deviceId) {
		super();
		this.eventName = eventName;
		this.customProperties = customProperties;
		this.operatingSystem = operatingSystem;
		this.browser = browser;
		this.currentUrl = currentUrl;
		this.browserVersion = browserVersion;
		this.screenHeight = screenHeight;
		this.screenWidth = screenWidth;
		this.deviceId = deviceId;
	}

	public String getEventName() {
		return eventName;
	}

	public void setEventName(String eventName) {
		this.eventName = eventName;
	}

	public Object getCustomProperties() {
		return customProperties;
	}

	public void setCustomProperties(Object customProperties) {
		this.customProperties = customProperties;
	}

	public String getOperatingSystem() {
		return operatingSystem;
	}

	public void setOperatingSystem(String operatingSystem) {
		this.operatingSystem = operatingSystem;
	}

	public String getBrowser() {
		return browser;
	}

	public void setBrowser(String browser) {
		this.browser = browser;
	}

	public String getCurrentUrl() {
		return currentUrl;
	}

	public void setCurrentUrl(String currentUrl) {
		this.currentUrl = currentUrl;
	}

	public String getBrowserVersion() {
		return browserVersion;
	}

	public void setBrowserVersion(String browserVersion) {
		this.browserVersion = browserVersion;
	}

	public String getScreenHeight() {
		return screenHeight;
	}

	public void setScreenHeight(String screenHeight) {
		this.screenHeight = screenHeight;
	}

	public String getScreenWidth() {
		return screenWidth;
	}

	public void setScreenWidth(String screenWidth) {
		this.screenWidth = screenWidth;
	}

	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

}
