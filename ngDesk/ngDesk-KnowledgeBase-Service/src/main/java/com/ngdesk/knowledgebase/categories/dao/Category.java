package com.ngdesk.knowledgebase.categories.dao;

import java.util.Date;
import java.util.List;

import javax.validation.constraints.Size;

import org.springframework.data.annotation.Id;

import com.ngdesk.commons.annotations.CustomNotEmpty;

public class Category {
	@Id
	private String categoryId;

	@CustomNotEmpty(message = "DAO_VARIABLE_REQUIRED", values = { "CATEGORY_NAME" })
	private String name;
	private String description;

	@CustomNotEmpty(message = "DAO_VARIABLE_REQUIRED", values = { "CATEGORY_SOURCED_LANGUAGE" })
	@Size(min = 2, max = 2, message = "LANGUAGE_MUST_BE_2_CHAR")
	private String sourceLanguage;

	private String createdBy;

	private String lastUpdatedBy;

	private Date dateCreated;

	private Date dateUpdated;

	private Boolean isDraft;

	private Integer order;

	@CustomNotEmpty(message = "DAO_VARIABLE_REQUIRED", values = { "VISIBLE_TO" })
	private List<String> visibleTo;

	public Category() {

	}

	public Category(String categoryId, String name, String description,
			@Size(min = 2, max = 2, message = "LANGUAGE_MUST_BE_2_CHAR") String sourceLanguage, String createdBy,
			String lastUpdatedBy, Date dateCreated, Date dateUpdated, Boolean isDraft, Integer order,
			List<String> visibleTo) {
		super();
		this.categoryId = categoryId;
		this.name = name;
		this.description = description;
		this.sourceLanguage = sourceLanguage;
		this.createdBy = createdBy;
		this.lastUpdatedBy = lastUpdatedBy;
		this.dateCreated = dateCreated;
		this.dateUpdated = dateUpdated;
		this.isDraft = isDraft;
		this.order = order;
		this.visibleTo = visibleTo;
	}

	public String getCategoryId() {
		return categoryId;
	}

	public void setCategoryId(String categoryId) {
		this.categoryId = categoryId;
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

	public String getSourceLanguage() {
		return sourceLanguage;
	}

	public void setSourceLanguage(String sourceLanguage) {
		this.sourceLanguage = sourceLanguage;
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

	public Boolean getIsDraft() {
		return isDraft;
	}

	public void setIsDraft(Boolean isDraft) {
		this.isDraft = isDraft;
	}

	public Integer getOrder() {
		return order;
	}

	public void setOrder(Integer order) {
		this.order = order;
	}

	public List<String> getVisibleTo() {
		return visibleTo;
	}

	public void setVisibleTo(List<String> visibleTo) {
		this.visibleTo = visibleTo;
	}

}
