package com.ngdesk.sam.swidtag;

import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ngdesk.commons.annotations.CustomNotEmpty;

public class Swidtag {

	@Id
	@JsonProperty("SWIDTAG_ID")
	private String swidtagId;

	@CustomNotEmpty(message = "DAO_VARIABLE_REQUIRED", values = { "FILE_NAME" })
	@JsonProperty("FILE_NAME")
	@Field("FILE_NAME")
	private String fileName;

	@CustomNotEmpty(message = "DAO_VARIABLE_REQUIRED", values = { "FILE_CONTENT" })
	@JsonProperty("FILE_CONTENT")
	@Field("FILE_CONTENT")
	private String fileContent;

	@JsonProperty("DATE_CREATED")
	@Field("DATE_CREATED")
	private Date dateCreated;

	@JsonProperty("DATE_UPDATED")
	@Field("DATE_UPDATED")
	private Date dateUpdated;

	@JsonProperty("CREATED_BY")
	@Field("CREATED_BY")
	private String createdBy;

	@JsonProperty("LAST_UPDATED_BY")
	@Field("LAST_UPDATED_BY")
	private String lastUpdatedBy;

	@JsonProperty("COMPANY_ID")
	@Field("COMPANY_ID")
	private String companyId;

	@CustomNotEmpty(message = "DAO_VARIABLE_REQUIRED", values = { "ASSET_ID" })
	@JsonProperty("ASSET_ID")
	@Field("ASSET_ID")
	private String assetId;

	@JsonProperty("STATUS")
	@Field("STATUS")
	private String status;

	Swidtag() {

	}

	public Swidtag(String swidtagId, String fileName, String fileContent, Date dateCreated, Date dateUpdated,
			String createdBy, String lastUpdatedBy, String companyId, String assetId, String status) {
		super();
		this.swidtagId = swidtagId;
		this.fileName = fileName;
		this.fileContent = fileContent;
		this.dateCreated = dateCreated;
		this.dateUpdated = dateUpdated;
		this.createdBy = createdBy;
		this.lastUpdatedBy = lastUpdatedBy;
		this.companyId = companyId;
		this.assetId = assetId;
		this.status = status;
	}

	public String getSwidtagId() {
		return swidtagId;
	}

	public void setSwidtagId(String swidtagId) {
		this.swidtagId = swidtagId;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getFileContent() {
		return fileContent;
	}

	public void setFileContent(String fileContent) {
		this.fileContent = fileContent;
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

	public String getCompanyId() {
		return companyId;
	}

	public void setCompanyId(String companyId) {
		this.companyId = companyId;
	}

	public String getAssetId() {
		return assetId;
	}

	public void setAssetId(String assetId) {
		this.assetId = assetId;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

}
