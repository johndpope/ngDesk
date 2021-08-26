package com.ngdesk.data.dao;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PublishDiscussionMessage {
	
	@JsonProperty("DISCUSSION_MESSAGE")
	private DiscussionMessage message;
	
	@JsonProperty("COMPANY_ID")
	private String companyId;
	
	public PublishDiscussionMessage() {
		
	}

	public PublishDiscussionMessage(DiscussionMessage message, String companyId) {
		super();
		this.message = message;
		this.companyId = companyId;
	}

	public DiscussionMessage getMessage() {
		return message;
	}

	public void setMessage(DiscussionMessage message) {
		this.message = message;
	}

	public String getCompanyId() {
		return companyId;
	}

	public void setCompanyId(String companyId) {
		this.companyId = companyId;
	}
	
}
