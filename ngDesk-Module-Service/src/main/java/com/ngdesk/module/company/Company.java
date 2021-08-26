package com.ngdesk.module.company;

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

	@JsonProperty("COMPANY_UUID")
	@Field("COMPANY_UUID")
	private String companyUuid;

	@JsonProperty("TIMEZONE")
	@Field("TIMEZONE")
	private String timezone;

	@JsonProperty("COMPANY_NAME")
	@Field("COMPANY_NAME")
	private String companyName;

	@JsonProperty("LANGUAGE")
	@Field("LANGUAGE")
	private String language;

	public Company() {

	}

	public Company(String companyId, String companySubdomain, String companyUuid, String timezone, String companyName,
			String language) {
		super();
		this.companyId = companyId;
		this.companySubdomain = companySubdomain;
		this.companyUuid = companyUuid;
		this.timezone = timezone;
		this.companyName = companyName;
		this.language = language;
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

	public String getCompanyName() {
		return companyName;
	}

	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

}
