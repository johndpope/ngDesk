package com.ngdesk.sam.rules.dao;

import java.util.Date;

import javax.validation.Valid;

import org.springframework.data.annotation.Id;

import com.ngdesk.commons.annotations.CustomNotEmpty;

public class SamFileRule {
	@Id
	private String id;

	@Valid
	private String fileName;

	@Valid
	private String filePath;

	@CustomNotEmpty(message = "DAO_VARIABLE_REQUIRED", values = { "SAM_FILE_RULE_RULE_CONDITION" })
	private String ruleCondition;

	private String hash;

	private String companyId;

	@CustomNotEmpty(message = "DAO_VARIABLE_REQUIRED", values = { "SAM_FILE_RULE_VERSION" })
	private String version;

	@CustomNotEmpty(message = "DAO_VARIABLE_REQUIRED", values = { "SAM_FILE_RULE_PUBLISHER" })
	private String publisher;

	private String edition;

	@CustomNotEmpty(message = "DAO_VARIABLE_REQUIRED", values = { "SAM_FILE_RULE_SOFTWARE_NAME" })
	private String softwareName;

	private Date dateCreated;

	private Date dateUpdated;

	private String createdBy;

	private String lastUpdatedBy;

	SamFileRule() {

	}

	public SamFileRule(String id, String fileName, String filePath, String ruleCondition, String hash, String companyId,
			String version, String publisher, String edition, String softwareName, Date dateCreated, Date dateUpdated,
			String createdBy, String lastUpdatedBy) {
		super();
		this.id = id;
		this.fileName = fileName;
		this.filePath = filePath;
		this.ruleCondition = ruleCondition;
		this.hash = hash;
		this.companyId = companyId;
		this.version = version;
		this.publisher = publisher;
		this.edition = edition;
		this.softwareName = softwareName;
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

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public String getRuleCondition() {
		return ruleCondition;
	}

	public void setRuleCondition(String ruleCondition) {
		this.ruleCondition = ruleCondition;
	}

	public String getHash() {
		return hash;
	}

	public void setHash(String hash) {
		this.hash = hash;
	}

	public String getCompanyId() {
		return companyId;
	}

	public void setCompanyId(String companyId) {
		this.companyId = companyId;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getPublisher() {
		return publisher;
	}

	public void setPublisher(String publisher) {
		this.publisher = publisher;
	}

	public String getEdition() {
		return edition;
	}

	public void setEdition(String edition) {
		this.edition = edition;
	}

	public String getSoftwareName() {
		return softwareName;
	}

	public void setSoftwareName(String softwareName) {
		this.softwareName = softwareName;
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