package com.ngdesk.websocket.channels.chat.dao;

import java.util.List;

import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ngdesk.data.dao.MessageAttachment;

public class ChatTicket {

	@JsonProperty("COMPANY_SUBDOMAIN")
	@Field("COMPANY_SUBDOMAIN")
	private String companySubdomain;

	@JsonProperty("SENDER")
	@Field("SENDER")
	private ChatTicketSender sender;

	@JsonProperty("SUBJECT")
	@Field("SUBJECT")
	private String subject;

	@JsonProperty("MESSAGE")
	@Field("MESSAGE")
	private String message;

	@JsonProperty("MESSAGE_TYPE")
	@Field("MESSAGE_TYPE")
	private String messageType;

	@JsonProperty("ATTACHMENTS")
	@Field("ATTACHMENTS")
	private List<MessageAttachment> attachments;

	@JsonProperty("SESSION_UUID")
	@Field("SESSION_UUID")
	private String sessionUUId;

	public ChatTicket() {

	}

	public ChatTicket(String companySubdomain, ChatTicketSender sender, String subject, String message,
			String messageType, List<MessageAttachment> attachments, String sessionUUId) {
		super();
		this.companySubdomain = companySubdomain;
		this.sender = sender;
		this.subject = subject;
		this.message = message;
		this.messageType = messageType;
		this.attachments = attachments;
		this.sessionUUId = sessionUUId;
	}

	public String getCompanySubdomain() {
		return companySubdomain;
	}

	public void setCompanySubdomain(String companySubdomain) {
		this.companySubdomain = companySubdomain;
	}

	public ChatTicketSender getSender() {
		return sender;
	}

	public void setSender(ChatTicketSender sender) {
		this.sender = sender;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
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

	public String getSessionUUId() {
		return sessionUUId;
	}

	public void setSessionUUId(String sessionUUId) {
		this.sessionUUId = sessionUUId;
	}

}
