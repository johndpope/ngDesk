package com.ngdesk.knowledgebase;

import java.sql.Timestamp;
import java.util.Date;

import javax.validation.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Comment {

	@JsonProperty("MESSAGE")
	@NotEmpty(message = "MESSAGE_REQUIRED")
	private String message;

	@JsonProperty("SENDER")
	private String sender;

	@JsonProperty("DATE_CREATED")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
	private Date dateCreated;

	@JsonProperty("MESSAGE_ID")
	private String messageId;

	public Comment() {

	}

	public Comment(@NotEmpty(message = "MESSAGE_REQUIRED") String message, String sender, Date dateCreated,
			String messageId) {
		super();
		this.message = message;
		this.sender = sender;
		this.dateCreated = dateCreated;
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

	public String getMessageId() {
		return messageId;
	}

	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}

}
