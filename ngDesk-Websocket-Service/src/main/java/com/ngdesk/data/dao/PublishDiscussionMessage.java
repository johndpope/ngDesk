package com.ngdesk.data.dao;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PublishDiscussionMessage {

	@JsonProperty("DISCUSSION_MESSAGE")
	private DiscussionMessage message;

	@JsonProperty("SUBDOMAIN")
	private String subdomain;

	@JsonProperty("USER_ID")
	private String userId;

	@JsonProperty("IS_TRIGGER")
	private boolean isTrigger;

	public PublishDiscussionMessage() {

	}

	public PublishDiscussionMessage(DiscussionMessage message, String subdomain, String userId, boolean isTrigger) {
		super();
		this.message = message;
		this.subdomain = subdomain;
		this.userId = userId;
		this.isTrigger = isTrigger;
	}

	public DiscussionMessage getMessage() {
		return message;
	}

	public void setMessage(DiscussionMessage message) {
		this.message = message;
	}

	public String getSubdomain() {
		return subdomain;
	}

	public void setSubdomain(String subdomain) {
		this.subdomain = subdomain;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public boolean isTrigger() {
		return isTrigger;
	}

	public void setTrigger(boolean isTrigger) {
		this.isTrigger = isTrigger;
	}

}
