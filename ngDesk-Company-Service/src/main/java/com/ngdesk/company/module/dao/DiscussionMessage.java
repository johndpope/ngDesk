package com.ngdesk.company.module.dao;

import java.sql.Timestamp;
import java.util.Date;

import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DiscussionMessage {

	@Field("CHAT_ID")
	@JsonProperty("CHAT_ID")
	private String chatId;

	@Field("MESSAGE")
	@JsonProperty("MESSAGE")
	private String message;

	@Field("SENDER")
	@JsonProperty("SENDER")
	private Sender sender;

	@Field("MODULE")
	@JsonProperty("MODULE")
	private String moduleId;

	@Field("DATE_CREATED")
	@JsonProperty("DATE_CREATED")
	private Date dateCreated = new Date();

	public DiscussionMessage() {

	}

	public DiscussionMessage(String chatId, String message, Sender sender, String moduleId, Date dateCreated) {
		super();
		this.chatId = chatId;
		this.message = message;
		this.sender = sender;
		this.moduleId = moduleId;
		this.dateCreated = dateCreated;
	}

	public String getChatId() {
		return chatId;
	}

	public void setChatId(String chatId) {
		this.chatId = chatId;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
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

	public Date getDateCreated() {
		return dateCreated;
	}

	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}

}
