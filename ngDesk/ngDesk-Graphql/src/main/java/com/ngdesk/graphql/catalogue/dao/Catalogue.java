package com.ngdesk.graphql.catalogue.dao;

import java.util.Date;
import java.util.List;

import org.springframework.data.annotation.Id;

public class Catalogue {

	@Id
	private String catalogueId;

	private String name;

	private List<CatalogueForm> catalogueForms;

	private String description;

	private String displayImage;

	private Date dateCreated;

	private Date dateUpdated;

	private String createdBy;

	private String lastUpdatedBy;
	
	private List<String> visibleTo;

	public Catalogue() {
		super();
	}

	public Catalogue(String catalogueId, String name, List<CatalogueForm> catalogueForms, String description,
			String displayImage, Date dateCreated, Date dateUpdated, String createdBy, String lastUpdatedBy, List<String> visibleTo) {
		super();
		this.catalogueId = catalogueId;
		this.name = name;
		this.catalogueForms = catalogueForms;
		this.description = description;
		this.displayImage = displayImage;
		this.dateCreated = dateCreated;
		this.dateUpdated = dateUpdated;
		this.createdBy = createdBy;
		this.lastUpdatedBy = lastUpdatedBy;
		this.visibleTo = visibleTo;
	}

	public String getId() {
		return catalogueId;
	}

	public void setId(String catalogueId) {
		this.catalogueId = catalogueId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<CatalogueForm> getForms() {
		return catalogueForms;
	}

	public void setForms(List<CatalogueForm> catalogueForms) {
		this.catalogueForms = catalogueForms;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDisplayImage() {
		return displayImage;
	}

	public void setDisplayImage(String displayImage) {
		this.displayImage = displayImage;
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

	public List<String> getVisibleTo() {
		return visibleTo;
	}

	public void setVisibleTo(List<String> visibleTo) {
		this.visibleTo = visibleTo;
	}


}