package com.ngdesk.blacklistwhitelist.emails;

import java.sql.Timestamp;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern.Flag;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

public class BlackListWhiteListEmail {
	@JsonProperty("BLACKLIST_WHITELIST_EMAIL_ID")
	private String blackListWhiteListEmailId;

	@JsonProperty("EMAIL_ADDRESS")
	@Email(flags = Flag.CASE_INSENSITIVE, message = "EMAIL_INVALID")
	private String emailAddress;

	@JsonProperty("TYPE")
	@NotNull(message = "BLACKLIST_WHITELIST_TYPE_NOT_NULL")
	private String type;

	@JsonProperty("STATUS")
	@NotNull(message = "BLACKLIST_WHITELIST_STATUS_NOT_NULL")
	private String status;

	@JsonProperty("DATE_CREATED")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
	private Timestamp dateCreated;

	@JsonProperty("DATE_UPDATED")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
	private Timestamp dateUpdated;

	@JsonProperty("LAST_UPDATED_BY")
	private String lastUpdatedBy;

	@JsonProperty("CREATED_BY")
	private String createdBy;

	@JsonProperty("IS_DOMAIN")
	private boolean domain;

	public BlackListWhiteListEmail() {

	}

	public BlackListWhiteListEmail(String blackListWhiteListEmailId,
			@Email(flags = Flag.CASE_INSENSITIVE, message = "EMAIL_INVALID") String emailAddress,
			@NotNull(message = "BLACKLIST_WHITELIST_TYPE_NOT_NULL") String type,
			@NotNull(message = "BLACKLIST_WHITELIST_STATUS_NOT_NULL") String status, Timestamp dateCreated,
			Timestamp dateUpdated, String lastUpdatedBy, String createdBy, boolean domain) {
		super();
		this.blackListWhiteListEmailId = blackListWhiteListEmailId;
		this.emailAddress = emailAddress;
		this.type = type;
		this.status = status;
		this.dateCreated = dateCreated;
		this.dateUpdated = dateUpdated;
		this.lastUpdatedBy = lastUpdatedBy;
		this.createdBy = createdBy;
		this.domain = domain;
	}

	public String getBlackListWhiteListEmailId() {
		return blackListWhiteListEmailId;
	}

	public void setBlackListWhiteListEmailId(String blackListWhiteListEmailId) {
		this.blackListWhiteListEmailId = blackListWhiteListEmailId;
	}

	public String getEmailAddress() {
		return emailAddress;
	}

	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Timestamp getDateCreated() {
		return dateCreated;
	}

	public void setDateCreated(Timestamp dateCreated) {
		this.dateCreated = dateCreated;
	}

	public Timestamp getDateUpdated() {
		return dateUpdated;
	}

	public void setDateUpdated(Timestamp dateUpdated) {
		this.dateUpdated = dateUpdated;
	}

	public String getLastUpdatedBy() {
		return lastUpdatedBy;
	}

	public void setLastUpdatedBy(String lastUpdatedBy) {
		this.lastUpdatedBy = lastUpdatedBy;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public boolean isDomain() {
		return domain;
	}

	public void setDomain(boolean domain) {
		this.domain = domain;
	}

}
