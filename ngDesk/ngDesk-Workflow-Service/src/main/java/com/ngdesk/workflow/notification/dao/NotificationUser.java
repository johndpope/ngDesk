package com.ngdesk.workflow.notification.dao;

import java.util.Date;

import nonapi.io.github.classgraph.json.Id;

public class NotificationUser {

	@Id
	private String id;

	private String messageType;

	private String companyId;

	private String moduleId;

	private String moduleName;

	private String dataId;

	private String recipientId;

	private Date dateCreated;

	private Date dateUpdated;

	private String message;

	private Boolean read;

	private String notificationUuid;

	public NotificationUser() {

	}

	public NotificationUser(String id, String messageType, String companyId, String moduleId, String moduleName,
			String dataId, String recipientId, Date dateCreated, Date dateUpdated, String message, Boolean read,
			String notificationUuid) {
		super();
		this.id = id;
		this.messageType = messageType;
		this.companyId = companyId;
		this.moduleId = moduleId;
		this.moduleName = moduleName;
		this.dataId = dataId;
		this.recipientId = recipientId;
		this.dateCreated = dateCreated;
		this.dateUpdated = dateUpdated;
		this.message = message;
		this.read = read;
		this.notificationUuid = notificationUuid;
	}

	public String getMessageType() {
		return messageType;
	}

	public void setMessageType(String messageType) {
		this.messageType = messageType;
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

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Boolean getRead() {
		return read;
	}

	public void setRead(Boolean read) {
		this.read = read;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getModuleName() {
		return moduleName;
	}

	public void setModuleName(String moduleName) {
		this.moduleName = moduleName;
	}

	public String getNotificationUuid() {
		return notificationUuid;
	}

	public void setNotificationUuid(String notificationUuid) {
		this.notificationUuid = notificationUuid;
	}

}
