package com.ngdesk.workflow.executor.dao;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;

public class BlackListWhiteList {

	@Id
	@JsonProperty("BLACKLIST_WHITELIST_EMAIL_ID")
	private String blackListWhiteListId;

	@JsonProperty("EMAIL_ADDRESS")
	@Field("EMAIL_ADDRESS")
	private String emailAddress;

	@JsonProperty("STATUS")
	@Field("STATUS")
	private String status;

	@JsonProperty("IS_DOMAIN")
	@Field("IS_DOMAIN")
	private Boolean domain;

	@JsonProperty("TYPE")
	@Field("TYPE")
	private String type;

	public BlackListWhiteList() {

	}

	public BlackListWhiteList(String blackListWhiteListId, String emailAddress, String status, Boolean domain,
			String type) {
		super();
		this.blackListWhiteListId = blackListWhiteListId;
		this.emailAddress = emailAddress;
		this.status = status;
		this.domain = domain;
		this.type = type;
	}

	public String getBlackListWhiteListId() {
		return blackListWhiteListId;
	}

	public void setBlackListWhiteListId(String blackListWhiteListId) {
		this.blackListWhiteListId = blackListWhiteListId;
	}

	public String getEmailAddress() {
		return emailAddress;
	}

	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Boolean getDomain() {
		return domain;
	}

	public void setDomain(Boolean domain) {
		this.domain = domain;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

}
