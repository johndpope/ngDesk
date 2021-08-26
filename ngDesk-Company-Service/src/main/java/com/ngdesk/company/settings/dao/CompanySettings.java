package com.ngdesk.company.settings.dao;

import java.util.ArrayList;

import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;

public class CompanySettings {

	@Field("COMPANY_SUBDOMAIN")
	@Schema(description = "Company Subdomain", required = false, example = "dev1")
	@JsonProperty("COMPANY_SUBDOMAIN")
	private String companySubdomain;

	@Field("ACCOUNT_LEVEL_ACCESS")
	@Schema(description = "Account Level Access", required = false, example = "false")
	@JsonProperty("ACCOUNT_LEVEL_ACCESS")
	private boolean accountLevelAccess;

	@Field("ROLES_WITH_CHAT")
	@Schema(description = "Roles with chat access", required = false, example = "role Id")
	@JsonProperty("ROLES_WITH_CHAT")
	private ArrayList<String> rolesWithChat;

	@Field("MAX_CHATS_PER_AGENT")
	@Schema(description = "max agents per chat", required = false, example = "1")
	@JsonProperty("MAX_CHATS_PER_AGENT")
	private int maxChatsPerAgent;

	CompanySettings() {

	}

	public CompanySettings(String companySubdomain, boolean accountLevelAccess, ArrayList<String> rolesWithChat,
			int maxChatsPerAgent) {
		super();
		this.companySubdomain = companySubdomain;
		this.accountLevelAccess = accountLevelAccess;
		this.rolesWithChat = rolesWithChat;
		this.maxChatsPerAgent = maxChatsPerAgent;
	}

	public String getCompanySubdomain() {
		return companySubdomain;
	}

	public void setCompanySubdomain(String companySubdomain) {
		this.companySubdomain = companySubdomain;
	}

	public boolean isAccountLevelAccess() {
		return accountLevelAccess;
	}

	public void setAccountLevelAccess(boolean accountLevelAccess) {
		this.accountLevelAccess = accountLevelAccess;
	}

	public ArrayList<String> getRolesWithChat() {
		return rolesWithChat;
	}

	public void setRolesWithChat(ArrayList<String> rolesWithChat) {
		this.rolesWithChat = rolesWithChat;
	}

	public int getMaxChatsPerAgent() {
		return maxChatsPerAgent;
	}

	public void setMaxChatsPerAgent(int maxChatsPerAgent) {
		this.maxChatsPerAgent = maxChatsPerAgent;
	}

}
