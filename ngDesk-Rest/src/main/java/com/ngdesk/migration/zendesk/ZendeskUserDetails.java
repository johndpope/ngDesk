package com.ngdesk.migration.zendesk;

import java.sql.Timestamp;

import javax.validation.constraints.NotEmpty;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

@Component
public class ZendeskUserDetails {

	@JsonProperty("EMAIL_ADDRESS")
	@NotEmpty(message = "EMAIL_ADDRESS_NOT_EMPTY")
	private String emailAddress;

	@JsonProperty("API_TOKEN")
	@NotEmpty(message = "API_TOKEN_NOT_EMPTY")
	private String apiToken;

	@JsonProperty("SUBDOMAIN")
	@NotEmpty(message = "COMPANY_SUBDOMAIN_NOT_EMPTY")
	private String subDomain;

	@JsonProperty("IMPORT_TICKETS_FROM")
	private String importTicketsFrom;

	public ZendeskUserDetails() {
	}

	public ZendeskUserDetails(@NotEmpty(message = "EMAIL_ADDRESS_NOT_EMPTY") String emailAddress,
			@NotEmpty(message = "API_TOKEN_NOT_EMPTY") String apiToken,
			@NotEmpty(message = "COMPANY_SUBDOMAIN_NOT_EMPTY") String subDomain, String importTicketsFrom) {
		super();
		this.emailAddress = emailAddress;
		this.apiToken = apiToken;
		this.subDomain = subDomain;
		this.importTicketsFrom = importTicketsFrom;
	}

	public String getEmailAddress() {
		return emailAddress;
	}

	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}

	public String getApiToken() {
		return apiToken;
	}

	public void setApiToken(String apiToken) {
		this.apiToken = apiToken;
	}

	public String getSubDomain() {
		return subDomain;
	}

	public void setSubDomain(String subDomain) {
		this.subDomain = subDomain;
	}

	public String getImportTicketsFrom() {
		return importTicketsFrom;
	}

	public void setImportTicketsFrom(String importTicketsFrom) {
		this.importTicketsFrom = importTicketsFrom;
	}

}
