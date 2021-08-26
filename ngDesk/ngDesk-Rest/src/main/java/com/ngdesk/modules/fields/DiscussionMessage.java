package com.ngdesk.modules.fields;

import java.sql.Timestamp;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

public class DiscussionMessage {

	@JsonProperty("CHAT_ID")
	private String chatId;

	@JsonProperty("MESSAGE")
	private String message;

	@JsonProperty("SENDER")
	private Sender sender;

	@JsonProperty("MODULE")
	private String moduleId;

	@JsonProperty("DATE_CREATED")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
	private Timestamp dateCreated;

	public DiscussionMessage() {

	}

	public DiscussionMessage(String chatId, String message, Sender sender, String moduleId, Timestamp dateCreated) {
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

	public Timestamp getDateCreated() {
		return dateCreated;
	}

	public void setDateCreated(Timestamp dateCreated) {
		this.dateCreated = dateCreated;
	}

}
