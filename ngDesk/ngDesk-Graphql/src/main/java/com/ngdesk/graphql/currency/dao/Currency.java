package com.ngdesk.graphql.currency.dao;

import java.util.Date;
import java.util.List;

import org.springframework.data.annotation.Id;

import com.ngdesk.commons.annotations.CustomNotEmpty;

public class Currency {

	@Id
	private String currencyId;

	@CustomNotEmpty(message = "DAO_VARIABLE_REQUIRED", values = { "NAME" })
	private String currencyName;

	@CustomNotEmpty(message = "DAO_VARIABLE_REQUIRED", values = { "ISO CODE" })
	private String isoCode;

	@CustomNotEmpty(message = "DAO_VARIABLE_REQUIRED", values = { "CURRENCY SYMBOL" })
	private String currencySymbol;

	@CustomNotEmpty(message = "DAO_VARIABLE_REQUIRED", values = { "STATUS" })
	private String status;

	private Boolean isDefault;

	private List<String> teams;

	private Date dateCreated;

	private Date dateUpdated;

	private String createdBy;

	private String lastUpdatedBy;

	public Currency() {

	}

	public Currency(String currencyId, String currencyName, String isoCode, String currencySymbol, String status,
			Boolean isDefault, List<String> teams, Date dateCreated, Date dateUpdated, String createdBy,
			String lastUpdatedBy) {
		super();
		this.currencyId = currencyId;
		this.currencyName = currencyName;
		this.isoCode = isoCode;
		this.currencySymbol = currencySymbol;
		this.status = status;
		this.isDefault = isDefault;
		this.teams = teams;
		this.dateCreated = dateCreated;
		this.dateUpdated = dateUpdated;
		this.createdBy = createdBy;
		this.lastUpdatedBy = lastUpdatedBy;
	}

	public String getCurrencyId() {
		return currencyId;
	}

	public void setCurrencyId(String currencyId) {
		this.currencyId = currencyId;
	}

	public String getCurrencyName() {
		return currencyName;
	}

	public void setCurrencyName(String currencyName) {
		this.currencyName = currencyName;
	}

	public String getIsoCode() {
		return isoCode;
	}

	public void setIsoCode(String isoCode) {
		this.isoCode = isoCode;
	}

	public String getCurrencySymbol() {
		return currencySymbol;
	}

	public void setCurrencySymbol(String currencySymbol) {
		this.currencySymbol = currencySymbol;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Boolean getIsDefault() {
		return isDefault;
	}

	public void setIsDefault(Boolean isDefault) {
		this.isDefault = isDefault;
	}

	public List<String> getTeams() {
		return teams;
	}

	public void setTeams(List<String> teams) {
		this.teams = teams;
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
