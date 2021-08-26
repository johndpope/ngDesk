package com.ngdesk.data.currency.dao;

import java.util.Date;
import java.util.List;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.springframework.data.annotation.Id;

import com.ngdesk.commons.annotations.CustomNotEmpty;

public class Currency {
	@Id
	private String currencyId;

	@CustomNotEmpty(message = "DAO_VARIABLE_REQUIRED", values = { "NAME" })
	private String currencyName;

	@Pattern(regexp = "^[A-Z]*$", message = "INVALID_ISO_CODE")
	@Size(min = 3, max = 3, message = "INVALID_ISO_CODE")
	@CustomNotEmpty(message = "DAO_VARIABLE_REQUIRED", values = { "ISO CODE" })
	private String isoCode;

	@Size(min = 1, max = 1, message = "INVALID_SYMBOL")
	@CustomNotEmpty(message = "DAO_VARIABLE_REQUIRED", values = { "CURRENCY SYMBOL" })
	private String currencySymbol;

	@Pattern(regexp = "Active|Inactive", message = "INVALID_STATUS")
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

	public Currency(String currencyId, String currencyName,
			@Pattern(regexp = "^[A-Z]*$", message = "INVALID_ISO_CODE") @Size(min = 3, max = 3, message = "INVALID_ISO_CODE") String isoCode,
			@Size(min = 1, max = 1, message = "INVALID_SYMBOL") String currencySymbol,
			@Pattern(regexp = "Active|Inactive", message = "INVALID_STATUS") String status, Boolean isDefault,
			List<String> teams, Date dateCreated, Date dateUpdated, String createdBy, String lastUpdatedBy) {
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

	public String getCurrencyName() {
		return currencyName;
	}

	public String getIsoCode() {
		return isoCode;
	}

	public String getCurrencySymbol() {
		return currencySymbol;
	}

	public String getStatus() {
		return status;
	}

	public Boolean getIsDefault() {
		return isDefault;
	}

	public List<String> getTeams() {
		return teams;
	}

	public Date getDateCreated() {
		return dateCreated;
	}

	public Date getDateUpdated() {
		return dateUpdated;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public String getLastUpdatedBy() {
		return lastUpdatedBy;
	}

	public void setCurrencyId(String currencyId) {
		this.currencyId = currencyId;
	}

	public void setCurrencyName(String currencyName) {
		this.currencyName = currencyName;
	}

	public void setIsoCode(String isoCode) {
		this.isoCode = isoCode;
	}

	public void setCurrencySymbol(String currencySymbol) {
		this.currencySymbol = currencySymbol;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public void setIsDefault(Boolean isDefault) {
		this.isDefault = isDefault;
	}

	public void setTeams(List<String> teams) {
		this.teams = teams;
	}

	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}

	public void setDateUpdated(Date dateUpdated) {
		this.dateUpdated = dateUpdated;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public void setLastUpdatedBy(String lastUpdatedBy) {
		this.lastUpdatedBy = lastUpdatedBy;
	}

}
