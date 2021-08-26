package com.ngdesk.graphql.notification.dao;

import java.util.Date;

import org.springframework.data.annotation.Id;


public class Notification {
	@Id
	private String notificationId;
	private String companyId;
	private String moduleId;
	private String dataId;
	private String recipientId;
	private Date dateCreated;
	private Date dateUpdated;
	private boolean read;
	private String message;
	
	public Notification() {
	
	}

	public Notification(String notificationId, String companyId, String moduleId, String dataId, String recipientId,
			Date dateCreated, Date dateUpdated, boolean read, String message) {
		super();
		this.notificationId = notificationId;
		this.companyId = companyId;
		this.moduleId = moduleId;
		this.dataId = dataId;
		this.recipientId = recipientId;
		this.dateCreated = dateCreated;
		this.dateUpdated = dateUpdated;
		this.read = read;
		this.message = message;
	}

	public String getNotificationId() {
		return notificationId;
	}

	public void setNotificationId(String notificationId) {
		this.notificationId = notificationId;
	}

	public String getCompanyId() {
		return companyId;
	}

	public void setCompanyId(String companyId) {
		this.companyId = companyId;
	}

	public String getModuleId() {
		return moduleId;
	}

	public void setModuleId(String moduleId) {
		this.moduleId = moduleId;
	}

	public String getDataId() {
		return dataId;
	}

	public void setDataId(String dataId) {
		this.dataId = dataId;
	}

	public String getRecipientId() {
		return recipientId;
	}

	public void setRecipientId(String recipientId) {
		this.recipientId = recipientId;
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

	public boolean isRead() {
		return read;
	}

	public void setRead(boolean read) {
		this.read = read;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	
}