package com.ngdesk.integration.docusign;

import java.util.Date;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Docusign {

	@Id
	@JsonProperty("DOCUSIGN_ID")
	private String docusignId;

	@JsonProperty("DOCUSIGN_AUTHENTICATION_DETAILS")
	@Field("DOCUSIGN_AUTHENTICATION_DETAILS")
	private AuthenticationDetails authenticationDetails;

	@JsonProperty("DOCUSIGN_USER_INFORMATION")
	@Field("DOCUSIGN_USER_INFORMATION")
	private DocusignUserInformation userInformation;

	@JsonProperty("COMPANY_ID")
	@Field("COMPANY_ID")
	public String companyId;

	@JsonProperty("DATE_CREATED")
	@Field("DATE_CREATED")
	private Date dateCreated;

	@JsonProperty("TOKEN_UPDATED_DATE")
	@Field("TOKEN_UPDATED_DATE")
	private Date tokenUpdatedDate;

	public Docusign() {

	}

	public Docusign(String docusignId, AuthenticationDetails authenticationDetails,
			DocusignUserInformation userInformation, String companyId, Date dateCreated, Date tokenUpdatedDate) {
		super();
		this.docusignId = docusignId;
		this.authenticationDetails = authenticationDetails;
		this.userInformation = userInformation;
		this.companyId = companyId;
		this.dateCreated = dateCreated;
		this.tokenUpdatedDate = tokenUpdatedDate;
	}

	public String getDocusignId() {
		return docusignId;
	}

	public void setDocusignId(String docusignId) {
		this.docusignId = docusignId;
	}

	public AuthenticationDetails getAuthenticationDetails() {
		return authenticationDetails;
	}

	public void setAuthenticationDetails(AuthenticationDetails authenticationDetails) {
		this.authenticationDetails = authenticationDetails;
	}

	public DocusignUserInformation getUserInformation() {
		return userInformation;
	}

	public void setUserInformation(DocusignUserInformation userInformation) {
		this.userInformation = userInformation;
	}

	public String getCompanyId() {
		return companyId;
	}

	public void setCompanyId(String companyId) {
		this.companyId = companyId;
	}

	public Date getDateCreated() {
		return dateCreated;
	}

	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}

	public Date getTokenUpdatedDate() {
		return tokenUpdatedDate;
	}

	public void setTokenUpdatedDate(Date tokenUpdatedDate) {
		this.tokenUpdatedDate = tokenUpdatedDate;
	}

}
