package com.ngdesk.chats;

import java.sql.Timestamp;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Message {

	@JsonProperty("CHAT_ID")
	private String chatId;

	@JsonProperty("SENDER")
	private ChatSender sender;

	@JsonProperty("MESSAGE")
	private String message;

	@JsonProperty("DATE_CREATED")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
	private Timestamp dateCreated;

	public Message() {

	}

	public Message(String chatId, ChatSender sender, String message, Timestamp dateCreated) {
		super();
		this.chatId = chatId;
		this.sender = sender;
		this.message = message;
		this.dateCreated = dateCreated;
	}

	public String getChatId() {
		return chatId;
	}

	public void setChatId(String chatId) {
		this.chatId = chatId;
	}

	public ChatSender getSender() {
		return sender;
	}

	public void setSender(ChatSender sender) {
		this.sender = sender;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Timestamp getDateCreated() {
		return dateCreated;
	}

	public void setDateCreated(Timestamp dateCreated) {
		this.dateCreated = dateCreated;
	}

}
