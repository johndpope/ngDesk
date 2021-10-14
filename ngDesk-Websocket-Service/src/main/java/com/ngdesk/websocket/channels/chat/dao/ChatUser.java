package com.ngdesk.websocket.channels.chat.dao;

public class ChatUser {

	private String firstName;

	private String lastName;

	private String emailAddress;

	private String sessionUUID;

	private String type;

	private String subdomain;

	public ChatUser() {

	}

	public ChatUser(String firstName, String lastName, String emailAddress, String sessionUUID, String type,
			String subdomain) {
		super();
		this.firstName = firstName;
		this.lastName = lastName;
		this.emailAddress = emailAddress;
		this.sessionUUID = sessionUUID;
		this.type = type;
		this.subdomain = subdomain;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
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

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getSubdomain() {
		return subdomain;
	}

	public void setSubdomain(String subdomain) {
		this.subdomain = subdomain;
	}

}
