package com.ngdesk.workflow.company.dao;

import java.util.ArrayList;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Company {

	@Id
	@JsonProperty("COMPANY_ID")
	private String companyId;

	@JsonProperty("COMPANY_SUBDOMAIN")
	@Field("COMPANY_SUBDOMAIN")
	private String companySubdomain;

	@JsonProperty("COMPANY_NAME")
	@Field("COMPANY_NAME")
	private String companyName;

	@JsonProperty("COMPANY_UUID")
	@Field("COMPANY_UUID")
	private String companyUuid;

	@JsonProperty("TIMEZONE")
	@Field("TIMEZONE")
	private String timezone;

	@JsonProperty("LANGUAGE")
	@Field("LANGUAGE")
	private String language;

	// TODO: REMOVE IF NOT REQURIED
	@JsonProperty("MAX_CHATS_PER_AGENT")
	@Field("MAX_CHATS_PER_AGENT")
	private int maxChatPerAgent = 5;

	@JsonProperty("ROLES_WITH_CHAT")
	@Field("ROLES_WITH_CHAT")
	private ArrayList<String> rolesWithChat;

	public Company() {

	}

	public Company(String companyId, String companySubdomain, String companyName, String companyUuid, String timezone,
			String language, int maxChatPerAgent, ArrayList<String> rolesWithChat) {
		super();
		this.companyId = companyId;
		this.companySubdomain = companySubdomain;
		this.companyName = companyName;
		this.companyUuid = companyUuid;
		this.timezone = timezone;
		this.language = language;
		this.maxChatPerAgent = maxChatPerAgent;
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

	public String getCompanyName() {
		return companyName;
	}

	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}

	public int getMaxChatPerAgent() {
		return maxChatPerAgent;
	}

	public void setMaxChatPerAgent(int maxChatPerAgent) {
		this.maxChatPerAgent = maxChatPerAgent;
	}

	public ArrayList<String> getRolesWithChat() {
		return rolesWithChat;
	}

	public void setRolesWithChat(ArrayList<String> rolesWithChat) {
		this.rolesWithChat = rolesWithChat;
	}

}
