package com.ngdesk.graphql.knowledgebase.section.dao;

import java.util.Date;
import java.util.List;

import org.springframework.data.annotation.Id;

public class Section {

	@Id
	private String sectionId;

	private String language;

	private String name;

	private String description;

	private String sortBy;

	private String category;

	private Date dateCreated;

	private Date dateUpdated;

	private String lastUpdatedBy;

	private String createdBy;

	private int order;

	private boolean isDraft;

	private List<String> visibleTo;

	private List<String> managedBy;

	public Section() {

	}

	public Section(String sectionId, String language, String name, String description, String sortBy, String category,
			Date dateCreated, Date dateUpdated, String lastUpdatedBy, String createdBy, int order, boolean isDraft,
			List<String> visibleTo, List<String> managedBy) {
		super();
		this.sectionId = sectionId;
		this.language = language;
		this.name = name;
		this.description = description;
		this.sortBy = sortBy;
		this.category = category;
		this.dateCreated = dateCreated;
		this.dateUpdated = dateUpdated;
		this.lastUpdatedBy = lastUpdatedBy;
		this.createdBy = createdBy;
		this.order = order;
		this.isDraft = isDraft;
		this.visibleTo = visibleTo;
		this.managedBy = managedBy;
	}

	public String getSectionId() {
		return sectionId;
	}

	public void setSectionId(String sectionId) {
		this.sectionId = sectionId;
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

	public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		this.order = order;
	}

	public boolean isDraft() {
		return isDraft;
	}

	public void setDraft(boolean isDraft) {
		this.isDraft = isDraft;
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
