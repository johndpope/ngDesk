package com.ngdesk.chats;

import javax.validation.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Participant {

	@JsonProperty("CHAT_ID")
	@NotEmpty(message = "CHAT_ID_REQUIRED")
	private String chatId;

	@JsonProperty("PARTICIPANT_ID")
	@NotEmpty(message = "PARTICIPANT_ID_REQUIRED")
	private String participantId;

	public Participant() {

	}

	public Participant(@NotEmpty(message = "CHAT_ID_REQUIRED") String chatId,
			@NotEmpty(message = "PARTICIPANT_ID_REQUIRED") String participantId) {
		super();
		this.chatId = chatId;
		this.participantId = participantId;
	}

	public String getChatId() {
		return chatId;
	}

	public void setChatId(String chatId) {
		this.chatId = chatId;
	}

	public String getParticipantId() {
		return participantId;
	}

	public void setParticipantId(String participantId) {
		this.participantId = participantId;
	}

}
