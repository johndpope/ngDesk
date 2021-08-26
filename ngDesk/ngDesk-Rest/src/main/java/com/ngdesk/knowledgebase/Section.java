package com.ngdesk.knowledgebase;

import java.util.Date;
import java.util.List;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Section {

	@JsonProperty("SECTION_ID")
	private String id;

	@JsonProperty("SOURCE_LANGUAGE")
	@NotEmpty(message = "LANGUAGE_NOT_NULL")
	@Size(min = 2, max = 2, message = "LANGUAGE_MUST_BE_2_CHAR")
	private String language;

	@JsonProperty("NAME")
	@NotEmpty(message = "SECTION_NAME_EMPTY")
	@NotBlank(message = "SECTION_NAME_EMPTY")
	@Size(max = 100, message = "INVALID_SECTION_NAME_SIZE")
	private String name;

	@JsonProperty("DESCRIPTION")
	@NotNull(message = "SECTION_DESCRIPTION_NOT_NULL")
	private String description;

	@JsonProperty("SORT_BY")
	@NotNull(message = "SORT_BY_NOT_NULL")
	@Pattern(regexp = "Manually|Manualmente", message = "INVALID_SORT_BY_FIELD")
	private String sortBy;

	@JsonProperty("CATEGORY")
	@NotEmpty(message = "SECTION_CATEGORY_EMPTY")
	private String category;

	@JsonProperty("DATE_CREATED")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
	private Date dateCreated;

	@JsonProperty("DATE_UPDATED")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
	private Date dateUpdated;

	@JsonProperty("LAST_UPDATED_BY")
	private String lastUpdated;

	@JsonProperty("CREATED_BY")
	private String created;

	@JsonProperty("ORDER")
	int order;

	@JsonProperty("IS_DRAFT")
	private boolean draft;

	@NotNull(message = "VISIBLE_TO_REQUIRED")
	@JsonProperty("VISIBLE_TO")
	private List<String> visibleTo;

	@NotNull(message = "MANAGED_BY")
	@JsonProperty("MANAGED_BY")
	private List<String> managedBy;

	public Section() {

	}

	public Section(String id,
			@NotEmpty(message = "LANGUAGE_NOT_NULL") @Size(min = 2, max = 2, message = "LANGUAGE_MUST_BE_2_CHAR") String language,
			@NotEmpty(message = "SECTION_NAME_EMPTY") @NotBlank(message = "SECTION_NAME_EMPTY") @Size(max = 100, message = "INVALID_SECTION_NAME_SIZE") String name,
			@NotNull(message = "SECTION_DESCRIPTION_NOT_NULL") String description,
			@NotNull(message = "SORT_BY_NOT_NULL") @Pattern(regexp = "Manually|Manualmente", message = "INVALID_SORT_BY_FIELD") String sortBy,
			@NotEmpty(message = "SECTION_CATEGORY_EMPTY") String category, Date dateCreated, Date dateUpdated,
			String lastUpdated, String created, int order, boolean draft,
			@NotNull(message = "VISIBLE_TO_REQUIRED") List<String> visibleTo,
			@NotNull(message = "MANAGED_BY") List<String> managedBy) {
		super();
		this.id = id;
		this.language = language;
		this.name = name;
		this.description = description;
		this.sortBy = sortBy;
		this.category = category;
		this.dateCreated = dateCreated;
		this.dateUpdated = dateUpdated;
		this.lastUpdated = lastUpdated;
		this.created = created;
		this.order = order;
		this.draft = draft;
		this.visibleTo = visibleTo;
		this.managedBy = managedBy;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
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

	public String getSortBy() {
		return sortBy;
	}

	public void setSortBy(String sortBy) {
		this.sortBy = sortBy;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
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

	public String getLastUpdated() {
		return lastUpdated;
	}

	public void setLastUpdated(String lastUpdated) {
		this.lastUpdated = lastUpdated;
	}

	public String getCreated() {
		return created;
	}

	public void setCreated(String created) {
		this.created = created;
	}

	public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		this.order = order;
	}

	public boolean isDraft() {
		return draft;
	}

	public void setDraft(boolean draft) {
		this.draft = draft;
	}

	public List<String> getVisibleTo() {
		return visibleTo;
	}

	public void setVisibleTo(List<String> visibleTo) {
		this.visibleTo = visibleTo;
	}

	public List<String> getManagedBy() {
		return managedBy;
	}

	public void setManagedBy(List<String> managedBy) {
		this.managedBy = managedBy;
	}

}
