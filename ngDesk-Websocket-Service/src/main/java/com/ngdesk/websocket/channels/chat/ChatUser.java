package com.ngdesk.websocket.channels.chat;

import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ChatUser {
	
	@JsonProperty("FIRST_NAME")
	@Field("FIRST_NAME")
	private String firstName;
	
	@JsonProperty("LAST_NAME")
	@Field("LAST_NAME")
	private String lastName;
	
	@JsonProperty("EMAIL_ADDRESS")
	@Field("EMAIL_ADDRESS")
	private String emailAddress;
	
	@JsonProperty("SESSION_UUID")
	@Field("SESSION_UUID")
	private String sessionUUID;
	
	@JsonProperty("MESSAGE_TYPE")
	@Field("MESSAGE_TYPE")
	private String messageType;
	
	@JsonProperty("SUB_DOMAIN")
	@Field("SUB_DOMAIN")
	private String subDomain;
	
	public ChatUser() {
		
	}

	public ChatUser(String firstname, String lastName, String emailAddress, String sessionUUID, String messageType,
			String subDomain) {
		super();
		this.firstName = firstname;
		this.lastName = lastName;
		this.emailAddress = emailAddress;
		this.sessionUUID = sessionUUID;
		this.messageType = messageType;
		this.subDomain = subDomain;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstname) {
		this.firstName = firstname;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getEmailAddress() {
		return emailAddress;
	}

	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}

	public String getSessionUUID() {
		return sessionUUID;
	}

	public void setSessionUUID(String sessionUUID) {
		this.sessionUUID = sessionUUID;
	}

	public String getMessageType() {
		return messageType;
	}

	public void setMessageType(String messageType) {
		this.messageType = messageType;
	}

	public String getSubDomain() {
		return subDomain;
	}

	public void setSubDomain(String subDomain) {
		this.subDomain = subDomain;
	}

}
