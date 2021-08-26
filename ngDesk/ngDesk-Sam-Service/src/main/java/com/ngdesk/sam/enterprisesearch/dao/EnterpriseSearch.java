package com.ngdesk.sam.enterprisesearch.dao;

import java.util.Date;
import java.util.List;

import javax.validation.constraints.Pattern;

import org.springframework.data.annotation.Id;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.ngdesk.commons.annotations.CustomNotEmpty;

public class EnterpriseSearch {

	@Id
	private String enterpriseSearchId;

	@CustomNotEmpty(message = "DAO_VARIABLE_REQUIRED", values = { "ENTERPRISE_SEARCH_NAME" })
	private String name;

	private String description;

	@CustomNotEmpty(message = "DAO_VARIABLE_REQUIRED", values = { "ENTERPRISE_SEARCH_TAGS" })

	private List<@Pattern(regexp = "Credit Card|Healthcare|Ssn|Address|Name|Postal Code", message = "INVALID_TAGS") String> tags;

	@CustomNotEmpty(message = "DAO_VARIABLE_REQUIRED", values = { "ENTERPRISE_SEARCH_FILE_PATH" })
	private String filePath;

	@CustomNotEmpty(message = "DAO_VARIABLE_REQUIRED", values = { "ENTERPRISE_SEARCH_REGEX" })
	private String regex;

	private String status;

	@JsonIgnore
	private String companyId;

	private Date dateCreated;

	private Date dateUpdated;

	private String createdBy;

	private String lastUpdatedBy;

	public EnterpriseSearch() {

	}

	public EnterpriseSearch(String enterpriseSearchId, String name, String description,
			List<@Pattern(regexp = "Credit Card|Healthcare|Ssn|Address|Name|Postal Code", message = "INVALID_TAGS") String> tags,
			String filePath, String regex, String status, String companyId, Date dateCreated, Date dateUpdated,
			String createdBy, String lastUpdatedBy) {
		super();
		this.enterpriseSearchId = enterpriseSearchId;
		this.name = name;
		this.description = description;
		this.tags = tags;
		this.filePath = filePath;
		this.regex = regex;
		this.status = status;
		this.companyId = companyId;
		this.dateCreated = dateCreated;
		this.dateUpdated = dateUpdated;
		this.createdBy = createdBy;
		this.lastUpdatedBy = lastUpdatedBy;
	}

	public String getEnterpriseSearchId() {
		return enterpriseSearchId;
	}

	public void setEnterpriseSearchId(String enterpriseSearchId) {
		this.enterpriseSearchId = enterpriseSearchId;
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

	public List<String> getTags() {
		return tags;
	}

	public void setTags(List<String> tags) {
		this.tags = tags;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public String getRegex() {
		return regex;
	}

	public void setRegex(String regex) {
		this.regex = regex;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
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
