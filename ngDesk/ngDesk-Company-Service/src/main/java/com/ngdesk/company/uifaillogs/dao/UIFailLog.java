package com.ngdesk.company.uifaillogs.dao;

import java.util.Date;

import org.bson.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UIFailLog {

	@JsonProperty("TYPE")
	@Field("TYPE")
	private String type;

	@JsonProperty("URL")
	@Field("URL")
	private String url;

	@JsonProperty("AUTHENTICATION_TOKEN")
	@Field("AUTHENTICATION_TOKEN")
	private String authenticationToken;

	@JsonProperty("BODY")
	@Field("BODY")
	private Document body;

	@JsonProperty("COMPANY_SUBDOMAIN")
	@Field("COMPANY_SUBDOMAIN")
	private String companySubdomain;

	@JsonProperty("DATE_CREATED")
	@Field("DATE_CREATED")
	private Date dateCreated = new Date();

	public UIFailLog(String type, String url, String authenticationToken, Document body, String companySubdomain,
			Date dateCreated) {
		super();
		this.type = type;
		this.url = url;
		this.authenticationToken = authenticationToken;
		this.body = body;
		this.companySubdomain = companySubdomain;
		this.dateCreated = dateCreated;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getAuthenticationToken() {
		return authenticationToken;
	}

	public void setAuthenticationToken(String authenticationToken) {
		this.authenticationToken = authenticationToken;
	}

	public Document getBody() {
		return body;
	}

	public void setBody(Document body) {
		this.body = body;
	}

	public String getCompanySubdomain() {
		return companySubdomain;
	}

	public void setCompanySubdomain(String companySubdomain) {
		this.companySubdomain = companySubdomain;
	}

	public Date getDateCreated() {
		return dateCreated;
	}

	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}

}
