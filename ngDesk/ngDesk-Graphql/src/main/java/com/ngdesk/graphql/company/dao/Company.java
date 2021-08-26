package com.ngdesk.graphql.company.dao;

import java.util.ArrayList;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;

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

	@Field("MAX_CHATS_PER_AGENT")
	private int maxChatsPerAgent;

	@Field("ROLES_WITH_CHAT")
	private ArrayList<String> rolesWithChat;

	public Company() {

	}

	public Company(String companyId, String companySubdomain, String companyName, String companyUuid, String timezone,
			String language, String accountLevelAccess, int maxChatsPerAgent, ArrayList<String> rolesWithChat) {
		super();
		this.companyId = companyId;
		this.companySubdomain = companySubdomain;
		this.companyName = companyName;
		this.companyUuid = companyUuid;
		this.timezone = timezone;
		this.language = language;
		this.accountLevelAccess = accountLevelAccess;
		this.maxChatsPerAgent = maxChatsPerAgent;
		this.rolesWithChat = rolesWithChat;
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

	public int getMaxChatsPerAgent() {
		return maxChatsPerAgent;
	}

	public void setMaxChatsPerAgent(int maxChatsPerAgent) {
		this.maxChatsPerAgent = maxChatsPerAgent;
	}

	public ArrayList<String> getRolesWithChat() {
		return rolesWithChat;
	}

	public void setRolesWithChat(ArrayList<String> rolesWithChat) {
		this.rolesWithChat = rolesWithChat;
	}

	

}
