package com.ngdesk.internalchat;

import java.sql.Date;
import java.sql.Timestamp;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ngdesk.discussion.Sender;

public class InternalChatMessage {

	@JsonProperty("MESSAGE")
	private String message;

	@JsonProperty("SENDER")
	private Sender sender;

	@JsonProperty("CHAT_ID")
	private String chatId;

	@JsonProperty("DATE_CREATED")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
	private Timestamp dateCreated;

	public InternalChatMessage() {

	}

	public InternalChatMessage(String message, Sender sender, String chatId, Timestamp dateCreated) {
		super();
		this.message = message;
		this.sender = sender;
		this.chatId = chatId;
		this.dateCreated = dateCreated;
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

	public String getChatId() {
		return chatId;
	}

	public void setChatId(String chatId) {
		this.chatId = chatId;
	}

	public Timestamp getDateCreated() {
		return dateCreated;
	}

	public void setDateCreated(Timestamp dateCreated) {
		this.dateCreated = dateCreated;
	}

}
