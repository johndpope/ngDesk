package com.ngdesk.websocket.companies.dao;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Company {

	@JsonProperty("COMPANY_ID")
	@Id
	private String id;

	@JsonProperty("COMPANY_SUBDOMAIN")
	@Field("COMPANY_SUBDOMAIN")
	private String companySubdomain;

	@JsonProperty("PHONE")
	private Phone phone;

	@JsonProperty("CHAT_SETTINGS")
	@Field("CHAT_SETTINGS")
	private ChatSettings chatSettings;

	@JsonProperty("COMPANY_UUID")
	@Field("COMPANY_UUID")
	private String companyUuid;

	public Company() {

	}

	public Company(String id, String companySubdomain, Phone phone, ChatSettings chatSettings, String companyUuid) {
		super();
		this.id = id;
		this.companySubdomain = companySubdomain;
		this.phone = phone;
		this.chatSettings = chatSettings;
		this.companyUuid = companyUuid;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getCompanySubdomain() {
		return companySubdomain;
	}

	public void setCompanySubdomain(String companySubdomain) {
		this.companySubdomain = companySubdomain;
	}

	public Phone getPhone() {
		return phone;
	}

	public void setPhone(Phone phone) {
		this.phone = phone;
	}

	public ChatSettings getChatSettings() {
		return chatSettings;
	}

	public void setChatSettings(ChatSettings chatSettings) {
		this.chatSettings = chatSettings;
	}

	public String getCompanyUuid() {
		return companyUuid;
	}

	public void setCompanyUuid(String companyUuid) {
		this.companyUuid = companyUuid;
	}

}
