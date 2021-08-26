package com.ngdesk.companies;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CompanyV1 {

	@JsonProperty("COMPANY_SUBDOMAIN")
	private String companySubdomain;

	@JsonProperty("EMAIL_ADDRESS")
	private String emailAddress;

	public CompanyV1() {

	}

	public CompanyV1(String companySubdomain, String emailAddress) {
		super();
		this.companySubdomain = companySubdomain;
		this.emailAddress = emailAddress;
	}

	public String getCompanySubdomain() {
		return companySubdomain;
	}

	public void setCompanySubdomain(String companySubdomain) {
		this.companySubdomain = companySubdomain;
	}

	public String getEmailAddress() {
		return emailAddress;
	}

	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}

}
