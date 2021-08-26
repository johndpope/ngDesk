package com.ngdesk.sam.company.dao;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonFormat;
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

	@JsonProperty("LANGUAGE")
	@Field("LANGUAGE")
	private String language;

	@JsonProperty("PRICING_TIER")
	@Field("PRICING_TIER")
	private String pricingTier;

	@JsonProperty("DATE_CREATED")
	@Field("DATE_CREATED")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
	private String dateCreated;

	@JsonProperty("DATE_UPDATED")
	@Field("DATE_UPDATED")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
	private String dateUpdated;

	@JsonProperty("COMPANY_NAME")
	@Field("COMPANY_NAME")
	private String companyName;

	@JsonProperty("LOCALE")
	@Field("LOCALE")
	private String locale;

	public Company() {

	}

	public Company(String companyId, String companySubdomain, String companyUuid, String language, String pricingTier,
			String dateCreated, String dateUpdated, String companyName, String locale) {
		super();
		this.companyId = companyId;
		this.companySubdomain = companySubdomain;
		this.companyUuid = companyUuid;
		this.language = language;
		this.pricingTier = pricingTier;
		this.dateCreated = dateCreated;
		this.dateUpdated = dateUpdated;
		this.companyName = companyName;
		this.locale = locale;
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

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public String getPricingTier() {
		return pricingTier;
	}

	public void setPricingTier(String pricingTier) {
		this.pricingTier = pricingTier;
	}

	public String getDateCreated() {
		return dateCreated;
	}

	public void setDateCreated(String dateCreated) {
		this.dateCreated = dateCreated;
	}

	public String getDateUpdated() {
		return dateUpdated;
	}

	public void setDateUpdated(String dateUpdated) {
		this.dateUpdated = dateUpdated;
	}

	public String getCompanyName() {
		return companyName;
	}

	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}

	public String getLocale() {
		return locale;
	}

	public void setLocale(String locale) {
		this.locale = locale;
	}

}
