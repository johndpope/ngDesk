package com.ngdesk.chats;

import java.sql.Timestamp;
import java.util.List;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

public class InternalChat {

	@JsonProperty("PARTICIPANTS")
	@NotNull(message = "PARTICIPANTS_MISSING")
	@Size(min = 2, message = "PARTICIPANTS_REQUIRED")
	private List<String> participants;

	@JsonProperty("MESSAGES")
	@NotNull(message = "MESSAGES_MISSING")
	private List<Message> messages;

	@JsonProperty("CHAT_ID")
	private String chatId;

	@JsonProperty("DATE_CREATED")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
	private Timestamp dateCreated;

	@JsonProperty("DATE_UPDATED")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
	private Timestamp dateUpdated;

	public InternalChat() {

	}

	public InternalChat(
			@NotNull(message = "PARTICIPANTS_MISSING") @Size(min = 2, message = "PARTICIPANTS_REQUIRED") List<String> participants,
			@NotNull(message = "MESSAGES_MISSING") List<Message> messages, String chatId, Timestamp dateCreated,
			Timestamp dateUpdated) {
		super();
		this.participants = participants;
		this.messages = messages;
		this.chatId = chatId;
		this.dateCreated = dateCreated;
		this.dateUpdated = dateUpdated;
	}

	public List<String> getParticipants() {
		return participants;
	}

	public void setParticipants(List<String> participants) {
		this.participants = participants;
	}

	public List<Message> getMessages() {
		return messages;
	}

	public void setMessages(List<Message> messages) {
		this.messages = messages;
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

	public Timestamp getDateUpdated() {
		return dateUpdated;
	}

	public void setDateUpdated(Timestamp dateUpdated) {
		this.dateUpdated = dateUpdated;
	}

}
