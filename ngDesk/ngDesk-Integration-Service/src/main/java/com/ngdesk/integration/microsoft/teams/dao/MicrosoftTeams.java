package com.ngdesk.integration.microsoft.teams.dao;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ngdesk.commons.annotations.CustomNotEmpty;

public class MicrosoftTeams {

	@Id
	@JsonProperty("TEAMS_ID")
	private String teamsId;

	@JsonProperty("TEAMS_CONTEXT_ACTIVITY")
	@Field("TEAMS_CONTEXT_ACTIVITY")
	@CustomNotEmpty(message = "DAO_VARIABLE_REQUIRED", values = { "TEAMS_CONTEXT_ACTIVITY" })
	public String teamsContextActivity;

	@JsonProperty("CHANNEL_ID")
	@Field("CHANNEL_ID")
	@CustomNotEmpty(message = "DAO_VARIABLE_REQUIRED", values = { "CHANNEL_ID" })
	public String channelId;

	@JsonProperty("CHANNEL_NAME")
	@Field("CHANNEL_NAME")
	@CustomNotEmpty(message = "DAO_VARIABLE_REQUIRED", values = { "CHANNEL_NAME" })
	public String channelName;

	@JsonProperty("AUTHENTICATED")
	@Field("AUTHENTICATED")
	private boolean authenticated;

	@JsonProperty("SUBDOMAIN")
	@Field("SUBDOMAIN")
	public String subdomain;

	@JsonProperty("COMPANY_ID")
	@Field("COMPANY_ID")
	public String companyId;

	@JsonProperty("EMAIL_ADDRESS")
	@Field("EMAIL_ADDRESS")
	public String emailAddress;

	public MicrosoftTeams() {

	}

	public MicrosoftTeams(String teamsId, String teamsContextActivity, String channelId, String channelName,
			boolean authenticated, String subdomain, String companyId, String emailAddress) {
		super();
		this.teamsId = teamsId;
		this.teamsContextActivity = teamsContextActivity;
		this.channelId = channelId;
		this.channelName = channelName;
		this.authenticated = authenticated;
		this.subdomain = subdomain;
		this.companyId = companyId;
		this.emailAddress = emailAddress;
	}

	public String getTeamsId() {
		return teamsId;
	}

	public void setTeamsId(String teamsId) {
		this.teamsId = teamsId;
	}

	public String getTeamsContextActivity() {
		return teamsContextActivity;
	}

	public void setTeamsContextActivity(String teamsContextActivity) {
		this.teamsContextActivity = teamsContextActivity;
	}

	public String getChannelId() {
		return channelId;
	}

	public void setChannelId(String channelId) {
		this.channelId = channelId;
	}

	public String getChannelName() {
		return channelName;
	}

	public void setChannelName(String channelName) {
		this.channelName = channelName;
	}

	public boolean isAuthenticated() {
		return authenticated;
	}

	public void setAuthenticated(boolean authenticated) {
		this.authenticated = authenticated;
	}

	public String getSubdomain() {
		return subdomain;
	}

	public void setSubdomain(String subdomain) {
		this.subdomain = subdomain;
	}

	public String getCompanyId() {
		return companyId;
	}

	public void setCompanyId(String companyId) {
		this.companyId = companyId;
	}

	public String getEmailAddress() {
		return emailAddress;
	}

	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}

}
