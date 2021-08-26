package com.ngdesk.publicticket;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PublicUserEntry {
	
	
	@JsonProperty("TO")
	@NotEmpty(message = "Receiver field cannot be empty")
	private String to;

	@JsonProperty("FROM")
	@NotEmpty(message = "Sender field cannot be empty")
	private String from;

	@JsonProperty("SUBJECT")
	@NotEmpty(message = "Subject field cannot be empty")
	private String subject;

	@JsonProperty("FIRST_NAME")
	@NotEmpty(message = "First name field cannot be empty")
	@Pattern(regexp="^[a-zA-Z]*$",message="Invalid First Name")
	private String firstName;

	@JsonProperty("LAST_NAME")
	@NotEmpty(message = "Last name field cannot be empty")
	@Pattern(regexp="^[a-zA-Z]*$",message="Invalid Last Name")
	private String lastName;

	@JsonProperty("BODY")
	@NotEmpty(message = "Content field cannot be empty")
	private String body;
	
	@JsonProperty("ATTACHMENTS")
	@Valid
	private List<PublicTicketAttachment> attachments;

	public PublicUserEntry() {
	}
	
	public PublicUserEntry(@NotEmpty(message = "Receiver field cannot be empty") String to,
			@NotEmpty(message = "Sender field cannot be empty") String from,
			@NotEmpty(message = "Subject field cannot be empty") String subject,
			@NotEmpty(message = "First name field cannot be empty") @Pattern(regexp = "^[a-zA-Z]*$", message = "Invalid First Name") String firstName,
			@NotEmpty(message = "Last name field cannot be empty") @Pattern(regexp = "^[a-zA-Z]*$", message = "Invalid Last Name") String lastName,
			@NotEmpty(message = "Content field cannot be empty") String body, @Valid List<PublicTicketAttachment> attachments) {
		super();
		this.to = to;
		this.from = from;
		this.subject = subject;
		this.firstName = firstName;
		this.lastName = lastName;
		this.body = body;
		this.attachments = attachments;
	}
	
	public String getTo() {
		return to;
	}

	public void setTo(String to) {
		this.to = to;
	}

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
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

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public List<PublicTicketAttachment> getAttachments() {
		return attachments;
	}

	public void setAttachments(List<PublicTicketAttachment> attachments) {
		this.attachments = attachments;
	}

}