package com.ngdesk.channels.chat;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UpdateNotification {

	@JsonProperty("COMPANY_UUID")
	private String companyUuid;
	
	@JsonProperty("USER_ID")
	private String userId;
	
	@JsonProperty("NOTIFICATION_UUID")
	private String notificationUuid;
	
	public UpdateNotification() {
		
	}

	public UpdateNotification(String companyUuid, String userId, String notificationUuid) {
		super();
		this.companyUuid = companyUuid;
		this.userId = userId;
		this.notificationUuid = notificationUuid;
	}

	public String getCompanyUuid() {
		return companyUuid;
	}

	public void setCompanyUuid(String companyUuid) {
		this.companyUuid = companyUuid;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getNotificationUuid() {
		return notificationUuid;
	}

	public void setNotificationUuid(String notificationUuid) {
		this.notificationUuid = notificationUuid;
	}
	
}
