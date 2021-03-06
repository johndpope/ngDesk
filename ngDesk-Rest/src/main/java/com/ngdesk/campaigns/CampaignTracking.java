package com.ngdesk.campaigns;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CampaignTracking {

	@JsonProperty("EMAIL_ADDRESS")
	private String emailAddress;
	
	@JsonProperty("COMPANY_ID")
	private String companyId;
	
	public CampaignTracking() {
		
	}

	public CampaignTracking(String emailAddress, String companyId) {
		super();
		this.emailAddress = emailAddress;
		this.companyId = companyId;
	}

	public String getEmailAddress() {
		return emailAddress;
	}

	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}

	public String getCompanyId() {
		return companyId;
	}

	public void setCompanyId(String companyId) {
		this.companyId = companyId;
	}
	
}
