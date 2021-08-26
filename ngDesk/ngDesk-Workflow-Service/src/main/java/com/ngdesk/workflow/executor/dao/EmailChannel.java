package com.ngdesk.workflow.executor.dao;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;

public class EmailChannel {

	@Id
	@JsonProperty("CHANNEL_ID")
	private String id;

	@JsonProperty("NAME")
	@Field("NAME")
	private String name;

	@JsonProperty("MODULE")
	@Field("MODULE")
	private String module;

	@JsonProperty("TYPE")
	@Field("TYPE")
	private String type;

	@JsonProperty("EMAIL_ADDRESS")
	@Field("EMAIL_ADDRESS")
	private String emailAddress;

	@JsonProperty("IS_VERIFIED")
	@Field("IS_VERIFIED")
	private Boolean verified;

	@JsonProperty("DESCRIPTION")
	@Field("DESCRIPTION")
	private String description;

	public EmailChannel() {

	}

	public EmailChannel(String id, String name, String module, String type, String emailAddress, Boolean verified,
			String description) {
		super();
		this.id = id;
		this.name = name;
		this.module = module;
		this.type = type;
		this.emailAddress = emailAddress;
		this.verified = verified;
		this.description = description;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getModule() {
		return module;
	}

	public void setModule(String module) {
		this.module = module;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getEmailAddress() {
		return emailAddress;
	}

	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}

	public Boolean getVerified() {
		return verified;
	}

	public void setVerified(Boolean verified) {
		this.verified = verified;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

}
