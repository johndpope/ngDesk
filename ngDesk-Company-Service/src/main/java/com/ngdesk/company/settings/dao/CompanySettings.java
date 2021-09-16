package com.ngdesk.company.settings.dao;

import javax.validation.Valid;

import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ngdesk.commons.annotations.CustomNotEmpty;
import com.ngdesk.commons.annotations.CustomTimeZoneValidation;

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

	@Schema(required = false, description = "Timezone of the company", example = "America/Chicago")
	@JsonProperty("TIMEZONE")
	@CustomNotEmpty(message = "DAO_VARIABLE_REQUIRED", values = { "COMPANY_TIMEZONE" })
	@CustomTimeZoneValidation(message = "INVALID_TIMEZONE", values = { "COMPANY_TIMEZONE" })
	@Field("TIMEZONE")
	private String timezone;

	@Field("CHAT_SETTINGS")
	@Schema(description = "Chat settings", required = false)
	@JsonProperty("CHAT_SETTINGS")
	@Valid
	private ChatSettings chatSettings;

	CompanySettings() {

	}

	public CompanySettings(String companySubdomain, boolean accountLevelAccess, String timezone,
			@Valid ChatSettings chatSettings) {
		super();
		this.companySubdomain = companySubdomain;
		this.accountLevelAccess = accountLevelAccess;
		this.timezone = timezone;
		this.chatSettings = chatSettings;
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

	public String getTimezone() {
		return timezone;
	}

	public void setTimezone(String timezone) {
		this.timezone = timezone;
	}

	public ChatSettings getChatSettings() {
		return chatSettings;
	}

	public void setChatSettings(ChatSettings chatSettings) {
		this.chatSettings = chatSettings;
	}

}
