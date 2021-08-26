package com.ngdesk.graphql.knowledgebase.article.dao;

import java.util.Date;

public class CommentMessage {

	private String messageId;
	private String message;
	private String sender;
	private Date dateCreated;

	public CommentMessage() {

	}

	public CommentMessage(String messageId, String message, String sender, Date dateCreated) {
		super();
		this.messageId = messageId;
		this.message = message;
		this.sender = sender;
		this.dateCreated = dateCreated;
	}

	public String getMessageId() {
		return messageId;
	}

	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getSender() {
		return sender;
	}

	public void setSender(String sender) {
		this.sender = sender;
	}

	public Date getDateCreated() {
		return dateCreated;
	}

	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}

}
