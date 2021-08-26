package com.ngdesk.data.dao;

import java.util.Date;
import java.util.List;

import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DiscussionMessage {

	@JsonProperty("MESSAGE")
	@Field("MESSAGE")
	private String message;

	@JsonProperty("DATE_CREATED")
	@Field("DATE_CREATED")
	private Date dateCreated;

	@JsonProperty("MESSAGE_ID")
	@Field("MESSAGE_ID")
	private String messageId;

	@JsonProperty("MESSAGE_TYPE")
	@Field("MESSAGE_TYPE")
	private String messageType;

	@JsonProperty("ATTACHMENTS")
	@Field("ATTACHMENTS")
	private List<MessageAttachment> attachments;

	@JsonProperty("SENDER")
	@Field("SENDER")
	private Sender sender;

	@JsonProperty("MODULE_ID")
	private String moduleId;

	@JsonProperty("ENTRY_ID")
	private String dataId;

	@JsonProperty("TYPE")
	private String type;

	public DiscussionMessage() {

	}

	public DiscussionMessage(String message, Date dateCreated, String messageId, String messageType,
			List<MessageAttachment> attachments, Sender sender, String moduleId, String dataId, String type) {
		super();
		this.message = message;
		this.dateCreated = dateCreated;
		this.messageId = messageId;
		this.messageType = messageType;
		this.attachments = attachments;
		this.sender = sender;
		this.moduleId = moduleId;
		this.dataId = dataId;
		this.type = type;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Date getDateCreated() {
		return dateCreated;
	}

	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}

	public String getMessageId() {
		return messageId;
	}

	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}

	public String getMessageType() {
		return messageType;
	}

	public void setMessageType(String messageType) {
		this.messageType = messageType;
	}

	public List<MessageAttachment> getAttachments() {
		return attachments;
	}

	public void setAttachments(List<MessageAttachment> attachments) {
		this.attachments = attachments;
	}

	public Sender getSender() {
		return sender;
	}

	public void setSender(Sender sender) {
		this.sender = sender;
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

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
}
