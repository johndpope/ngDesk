package com.ngdesk.knowledgebase;

import java.util.Date;
import java.util.List;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Category {

	@JsonProperty("NAME")
	@NotEmpty(message = "CATEGORY_NAME_CANNOT_BE_EMPTY")
	@NotBlank(message = "CATEGORY_NAME_CANNOT_BE_EMPTY")
	@Size(max = 100, message = "INVALID_CATEGORY_NAME_SIZE")
	private String name;

	@JsonProperty("DESCRIPTION")
	@NotNull(message = "CATEGORY_DESCRIPTION_CANNOT_BE_NULL")
	private String description;

	@JsonProperty("SOURCE_LANGUAGE")
	@NotEmpty(message = "LANGUAGE_NOT_NULL")
	@Size(min = 2, max = 2, message = "LANGUAGE_MUST_BE_2_CHAR")
	private String sourceLanguage;

	@JsonProperty("CATEGORY_ID")
	private String categoryId;

	@JsonProperty("DATE_CREATED")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
	private Date dateCreated;

	@JsonProperty("DATE_UPDATED")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
	private Date dateUpdated;

	@JsonProperty("CREATED_BY")
	private String createdBy;

	@JsonProperty("LAST_UPDATED_BY")
	private String lastUpdatedBy;

	@JsonProperty("IS_DRAFT")
	private boolean draft;

	@JsonProperty("ORDER")
	int order;

	@NotNull(message = "VISIBLE_TO_REQUIRED")
	@JsonProperty("VISIBLE_TO")
	private List<String> visibleTo;

	Category() {
	}

	public Category(
			@NotEmpty(message = "CATEGORY_NAME_CANNOT_BE_EMPTY") @NotBlank(message = "CATEGORY_NAME_CANNOT_BE_EMPTY") @Size(max = 100, message = "INVALID_CATEGORY_NAME_SIZE") String name,
			@NotNull(message = "CATEGORY_DESCRIPTION_CANNOT_BE_NULL") String description,
			@NotEmpty(message = "LANGUAGE_NOT_NULL") @Size(min = 2, max = 2, message = "LANGUAGE_MUST_BE_2_CHAR") String sourceLanguage,
			String categoryId, Date dateCreated, Date dateUpdated, String createdBy, String lastUpdatedBy,
			boolean draft, int order, @NotNull(message = "VISIBLE_TO_REQUIRED") List<String> visibleTo) {
		super();
		this.name = name;
		this.description = description;
		this.sourceLanguage = sourceLanguage;
		this.categoryId = categoryId;
		this.dateCreated = dateCreated;
		this.dateUpdated = dateUpdated;
		this.createdBy = createdBy;
		this.lastUpdatedBy = lastUpdatedBy;
		this.draft = draft;
		this.order = order;
		this.visibleTo = visibleTo;
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

	public String getCategoryId() {
		return categoryId;
	}

	public void setCategoryId(String categoryId) {
		this.categoryId = categoryId;
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

	public boolean isDraft() {
		return draft;
	}

	public void setDraft(boolean draft) {
		this.draft = draft;
	}

	public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		this.order = order;
	}

	public List<String> getVisibleTo() {
		return visibleTo;
	}

	public void setVisibleTo(List<String> visibleTo) {
		this.visibleTo = visibleTo;
	}

}
