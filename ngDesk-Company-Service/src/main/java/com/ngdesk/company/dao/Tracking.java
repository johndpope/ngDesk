package com.ngdesk.company.dao;

import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Tracking {
	@JsonProperty("UTM_SOURCE")
	@Field("UTM_SOURCE")
	private String utmSource;

	@JsonProperty("UTM_MEDIUM")
	@Field("UTM_MEDIUM")
	private String utmMedium;

	@JsonProperty("UTM_CAMPAIGN")
	@Field("UTM_CAMPAIGN")
	private String utmCampaign;

	@JsonProperty("UTM_TERM")
	@Field("UTM_TERM")
	private String utmTeam;

	@JsonProperty("UTM_CONTENT")
	@Field("UTM_CONTENT")
	private String utmContent;

	public Tracking(String utmSource, String utmMedium, String utmCampaign, String utmTeam, String utmContent) {
		super();
		this.utmSource = utmSource;
		this.utmMedium = utmMedium;
		this.utmCampaign = utmCampaign;
		this.utmTeam = utmTeam;
		this.utmContent = utmContent;
	}

	public Tracking() {

	}

	public String getUtmSource() {
		return utmSource;
	}

	public String getUtmMedium() {
		return utmMedium;
	}

	public String getUtmCampaign() {
		return utmCampaign;
	}

	public String getUtmTeam() {
		return utmTeam;
	}

	public String getUtmContent() {
		return utmContent;
	}

	public void setUtmSource(String utmSource) {
		this.utmSource = utmSource;
	}

	public void setUtmMedium(String utmMedium) {
		this.utmMedium = utmMedium;
	}

	public void setUtmCampaign(String utmCampaign) {
		this.utmCampaign = utmCampaign;
	}

	public void setUtmTeam(String utmTeam) {
		this.utmTeam = utmTeam;
	}

	public void setUtmContent(String utmContent) {
		this.utmContent = utmContent;
	}

}
