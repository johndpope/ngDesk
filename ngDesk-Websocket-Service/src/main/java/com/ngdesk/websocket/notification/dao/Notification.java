package com.ngdesk.websocket.notification.dao;

import java.util.Date;

import javax.validation.constraints.NotEmpty;

import org.springframework.data.annotation.Id;

public class Notification {

	@Id
	private String id;

	private String companyId;

	private String moduleId;

	private String dataId;

	private String recipientId;

	private Date dateCreated;

	private Date dateUpdated;

	private String message;

	private Boolean read;

	public Notification() {
		super();
	}

	public Notification(String companyId, String moduleId, String dataId, String recipientId, Date dateCreated,
			Date dateUpdated, Boolean read, @NotEmpty(message = "DAO_VARIABLE_REQUIRED") String message) {
		super();

		this.companyId = companyId;
		this.moduleId = moduleId;
		this.dataId = dataId;
		this.recipientId = recipientId;
		this.dateCreated = dateCreated;
		this.dateUpdated = dateUpdated;
		this.read = read;
		this.message = message;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
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

	public Boolean getRead() {
		return read;
	}

	public void setRead(Boolean read) {
		this.read = read;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

}
