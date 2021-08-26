package com.ngdesk;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ngdesk.flowmanager.Attachment;

public class IncomingEmail {
	
	@JsonProperty("ATTACHMENTS")
	private List<Attachment> attachments;
	
	@JsonProperty("EMAIL_ADDRESS")
	private List<EmailAddress> emailAddresses;
	
	@JsonProperty("SUBJECT")
	private String subject;
	
	@JsonProperty("FROM")
	private String from;
	
	@JsonProperty("LAST_NAME")
	private String lastName;
	
	@JsonProperty("BODY_HTML")
	private String bodyHtml;
	
	@JsonProperty("BODY")
	private String body;
	
	@JsonProperty("FIRST_NAME")
	private String firstName;

	public IncomingEmail() {

	}

	public IncomingEmail(String sender, List<Attachment> attachments, List<EmailAddress> ccEmails, String subject,
			String from, String lastName, String bodyHtml, String body, String firstName) {
		super();
		this.attachments = attachments;
		this.emailAddresses = ccEmails;
		this.subject = subject;
		this.from = from;
		this.lastName = lastName;
		this.bodyHtml = bodyHtml;
		this.body = body;
		this.firstName = firstName;
	}

	public List<Attachment> getAttachments() {
		return attachments;
	}

	public void setAttachments(List<Attachment> attachments) {
		this.attachments = attachments;
	}

	public List<EmailAddress> getEmailAddresses() {
		return emailAddresses;
	}

	public void setEmailAddresses(List<EmailAddress> emailAddresses) {
		this.emailAddresses = emailAddresses;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getBodyHtml() {
		return bodyHtml;
	}
	public void setBodyHtml(String bodyHtml) {
		this.bodyHtml = bodyHtml;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

}
