package com.ngdesk.sam.discoverymap.dao;

import java.util.Date;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.Pattern;

import org.springframework.data.annotation.Id;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.ngdesk.commons.annotations.CustomNotEmpty;
import com.ngdesk.sam.normalizationrules.dao.Rule;

public class DiscoveryMap {

	@Id
	private String id;

	@CustomNotEmpty(message = "DAO_VARIABLE_REQUIRED", values = { "NAME" })
	private String name;

	private String description;

	private String number;

	private List<String> products;

	@Valid
	private Rule version;

	@Valid
	private Rule edition;

	@Pattern(regexp = "Windows|Linux|Mac", message = "INVALID_PLATFORM")
	private String platform;

	private String language;

	private String approved;

	private String companyId;

	private Date dateCreated;

	private Date dateUpdated;

	private String createdBy;

	private String lastUpdatedBy;

	DiscoveryMap() {

	}

	public DiscoveryMap(String id, String name, String description, String number, List<String> products,
			@Valid Rule version, @Valid Rule edition,
			@Pattern(regexp = "Windows|Linux|Mac", message = "INVALID_PLATFORM") String platform, String language,
			String approved, String companyId, Date dateCreated, Date dateUpdated, String createdBy,
			String lastUpdatedBy) {
		super();
		this.id = id;
		this.name = name;
		this.description = description;
		this.number = number;
		this.products = products;
		this.version = version;
		this.edition = edition;
		this.platform = platform;
		this.language = language;
		this.approved = approved;
		this.companyId = companyId;
		this.dateCreated = dateCreated;
		this.dateUpdated = dateUpdated;
		this.createdBy = createdBy;
		this.lastUpdatedBy = lastUpdatedBy;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
	}

	public List<String> getProducts() {
		return products;
	}

	public void setProducts(List<String> products) {
		this.products = products;
	}

	public Rule getVersion() {
		return version;
	}

	public void setVersion(Rule version) {
		this.version = version;
	}

	public Rule getEdition() {
		return edition;
	}

	public void setEdition(Rule edition) {
		this.edition = edition;
	}

	public String getPlatform() {
		return platform;
	}

	public void setPlatform(String platform) {
		this.platform = platform;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public String getApproved() {
		return approved;
	}

	public void setApproved(String approved) {
		this.approved = approved;
	}

	public String getCompanyId() {
		return companyId;
	}

	public void setCompanyId(String companyId) {
		this.companyId = companyId;
	}

	public Date getDateCreated() {
		return dateCreated;
	}

	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}

	public Date getDateUpdated() {
		return dateUpdated;
	}

	public void setDateUpdated(Date dateUpdated) {
		this.dateUpdated = dateUpdated;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public String getLastUpdatedBy() {
		return lastUpdatedBy;
	}

	public void setLastUpdatedBy(String lastUpdatedBy) {
		this.lastUpdatedBy = lastUpdatedBy;
	}

	

}
