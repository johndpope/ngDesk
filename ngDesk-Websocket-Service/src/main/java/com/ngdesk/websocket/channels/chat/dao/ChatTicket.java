package com.ngdesk.websocket.channels.chat.dao;

import java.util.List;

import com.ngdesk.data.dao.MessageAttachment;

public class ChatTicket {

	private String companySubdomain;

	private ChatTicketSender sender;

	private String subject;

	private String message;

	private String type;

	private List<MessageAttachment> attachments;

	private String sessionUUId;

	public ChatTicket() {

	}

	public ChatTicket(String companySubdomain, ChatTicketSender sender, String subject, String message, String type,
			List<MessageAttachment> attachments, String sessionUUId) {
		super();
		this.companySubdomain = companySubdomain;
		this.sender = sender;
		this.subject = subject;
		this.message = message;
		this.type = type;
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

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
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
