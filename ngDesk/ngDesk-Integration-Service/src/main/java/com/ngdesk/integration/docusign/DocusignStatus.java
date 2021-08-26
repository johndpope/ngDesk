package com.ngdesk.integration.docusign;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DocusignStatus {

	@JsonProperty("DOCUSIGN_AUTHENTICATED")
	private Boolean docusignAuthenticated;

	@JsonProperty("DOCUSIGN_USER_INFORMATION")
	private DocusignUserInformation userInformation;

	public DocusignStatus() {

	}

	public DocusignStatus(Boolean docusignAuthenticated, DocusignUserInformation userInformation) {
		super();
		this.docusignAuthenticated = docusignAuthenticated;
		this.userInformation = userInformation;
	}

	public Boolean getDocusignAuthenticated() {
		return docusignAuthenticated;
	}

	public void setDocusignAuthenticated(Boolean docusignAuthenticated) {
		this.docusignAuthenticated = docusignAuthenticated;
	}

	public DocusignUserInformation getUserInformation() {
		return userInformation;
	}

	public void setUserInformation(DocusignUserInformation userInformation) {
		this.userInformation = userInformation;
	}

}
