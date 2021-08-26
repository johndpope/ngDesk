package com.ngdesk.channels.email;

import com.fasterxml.jackson.annotation.JsonProperty;

public class EmailMapping {

	@JsonProperty("BODY")
	private String body;

	@JsonProperty("REQUESTOR")
	private String requestor;

	@JsonProperty("SUBJECT")
	private String subject;
	
	@JsonProperty("CC_EMAILS")
	private String ccemails;
	
	@JsonProperty("FROM_EMAIL")
	private String fromEmail;
	
	@JsonProperty("TEAMS")
	private String teams;

	public EmailMapping() {
	}

	public EmailMapping(String body, String requestor, String subject, String ccemails, String fromEmail,
			String teams) {
		super();
		this.body = body;
		this.requestor = requestor;
		this.subject = subject;
		this.ccemails = ccemails;
		this.fromEmail = fromEmail;
		this.teams = teams;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public String getRequestor() {
		return requestor;
	}

	public void setRequestor(String requestor) {
		this.requestor = requestor;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getCcemails() {
		return ccemails;
	}

	public void setCcemails(String ccemails) {
		this.ccemails = ccemails;
	}

	public String getFromEmail() {
		return fromEmail;
	}

	public void setFromEmail(String fromEmail) {
		this.fromEmail = fromEmail;
	}

	public String getTeams() {
		return teams;
	}

	public void setTeams(String teams) {
		this.teams = teams;
	}


	

			
}
