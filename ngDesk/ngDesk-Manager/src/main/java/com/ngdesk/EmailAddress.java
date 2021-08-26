package com.ngdesk;

import com.fasterxml.jackson.annotation.JsonProperty;

public class EmailAddress {

	@JsonProperty("EMAIL")
	private String email;
	
	@JsonProperty("IS_CC")
	private Boolean isCc;
	
	public EmailAddress() {
		
	}
	
	public EmailAddress(String email, Boolean isCc) {
		super();
		this.email = email;
		this.isCc = isCc;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public Boolean getIsCc() {
		return isCc;
	}

	public void setIsCc(Boolean isCc) {
		this.isCc = isCc;
	}
	
	
	
	

}
