package com.ngdesk.companies;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Referral {

	
	@JsonProperty("COMPANY_UUID")
	private String companyUuid;
	
	@JsonProperty("USER_UUID")
	private String userUuid;
	
	public Referral() {
		
		
	}

	public Referral(String companyUuid, String userUuid) {
		super();
		this.companyUuid = companyUuid;
		this.userUuid = userUuid;
	}

	public String getCompanyUuid() {
		return companyUuid;
	}

	public String getUserUuid() {
		return userUuid;
	}

	public void setCompanyUuid(String companyUuid) {
		this.companyUuid = companyUuid;
	}

	public void setUserUuid(String userUuid) {
		this.userUuid = userUuid;
	}
	
	
	
}
