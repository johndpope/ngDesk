package com.ngdesk.modules.detail.mobile.layouts;

import java.util.Date;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

public class MobileLayout {

	@JsonProperty("LAYOUT_ID")
	private String mobileLayoutId;

	@JsonProperty("NAME")
	@NotNull(message = "MOBILELAYOUT_NAME_NOT_NULL")
	@Size(min = 1, message = "MOBILELAYOUT_NAME_NOT_EMPTY")
	private String name;

	@JsonProperty("DESCRIPTION")
	@NotNull(message = "DESCRIPTION_NOT_NULL")
	private String description;

	@JsonProperty("FIELDS")
	@NotNull(message = "COLUMNS_NOT_NULL")
	@Valid
	private List<String> fields;

	@JsonProperty("ROLE")
	@NotNull(message = "ROLE_NOT_NULL")
	private String role;

	@JsonProperty("DATE_CREATED")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
	private Date dateCreated;

	@JsonProperty("DATE_UPDATED")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
	private Date dateUpdated;

	@JsonProperty("LAST_UPDATED_BY")
	private String lastUpdatedBy;

	@JsonProperty("CREATED_BY")
	private String createdBy;

	public MobileLayout() {

	}

	public MobileLayout(String mobileLayoutId,
			@NotNull(message = "MOBILELAYOUT_NAME_NOT_NULL") @Size(min = 1, message = "MOBILELAYOUT_NAME_NOT_EMPTY") String name,
			@NotNull(message = "DESCRIPTION_NOT_NULL") String description,
			@NotNull(message = "COLUMNS_NOT_NULL") @Valid List<String> fields,
			@NotNull(message = "ROLE_NOT_NULL") String role, Date dateCreated, Date dateUpdated, String lastUpdatedBy,
			String createdBy) {
		super();
		this.mobileLayoutId = mobileLayoutId;
		this.name = name;
		this.description = description;
		this.fields = fields;
		this.role = role;
		this.dateCreated = dateCreated;
		this.dateUpdated = dateUpdated;
		this.lastUpdatedBy = lastUpdatedBy;
		this.createdBy = createdBy;
	}

	public String getMobileLayoutId() {
		return mobileLayoutId;
	}

	public void setMobileLayoutId(String mobileLayoutId) {
		this.mobileLayoutId = mobileLayoutId;
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

	public List<String> getFields() {
		return fields;
	}

	public void setFields(List<String> fields) {
		this.fields = fields;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
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

}
