package com.ngdesk.graphql.company.dao;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;

public class Company {

	@Id
	private String companyId;

	@Field("COMPANY_SUBDOMAIN")
	private String companySubdomain;

	@Field("COMPANY_NAME")
	private String companyName;

	@Field("COMPANY_UUID")
	private String companyUuid;

	@Field("TIMEZONE")
	private String timezone;

	@Field("LANGUAGE")
	private String language;

	@Field("ACCOUNT_LEVEL_ACCESS")
	private String accountLevelAccess;

	@Field("CHAT_SETTINGS")
	private ChatSettings chatSettings;

	@Field("MAX_CHATS_PER_AGENT")
	private int maxChatsPerAgent;

	public Company() {

	}

	public Company(String companyId, String companySubdomain, String companyName, String companyUuid, String timezone,
			String language, String accountLevelAccess, ChatSettings chatSettings, int maxChatsPerAgent) {
		super();
		this.companyId = companyId;
		this.companySubdomain = companySubdomain;
		this.companyName = companyName;
		this.companyUuid = companyUuid;
		this.timezone = timezone;
		this.language = language;
		this.accountLevelAccess = accountLevelAccess;
		this.chatSettings = chatSettings;
		this.maxChatsPerAgent = maxChatsPerAgent;
	}

	public String getCompanyId() {
		return companyId;
	}

	public void setCompanyId(String companyId) {
		this.companyId = companyId;
	}

	public String getCompanySubdomain() {
		return companySubdomain;
	}

	public void setCompanySubdomain(String companySubdomain) {
		this.companySubdomain = companySubdomain;
	}

	public String getCompanyName() {
		return companyName;
	}

	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}

	public String getCompanyUuid() {
		return companyUuid;
	}

	public void setCompanyUuid(String companyUuid) {
		this.companyUuid = companyUuid;
	}

	public String getTimezone() {
		return timezone;
	}

	public void setTimezone(String timezone) {
		this.timezone = timezone;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public String getAccountLevelAccess() {
		return accountLevelAccess;
	}

	public void setAccountLevelAccess(String accountLevelAccess) {
		this.accountLevelAccess = accountLevelAccess;
	}

	public ChatSettings getChatSettings() {
		return chatSettings;
	}

	public void setChatSettings(ChatSettings chatSettings) {
		this.chatSettings = chatSettings;
	}

	public int getMaxChatsPerAgent() {
		return maxChatsPerAgent;
	}

	public void setMaxChatsPerAgent(int maxChatsPerAgent) {
		this.maxChatsPerAgent = maxChatsPerAgent;
	}

}
