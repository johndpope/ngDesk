package com.ngdesk.graphql.form.dao;

import java.util.Date;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.Pattern;

import org.springframework.data.annotation.Id;

import com.ngdesk.commons.annotations.CustomNotEmpty;
import com.ngdesk.commons.annotations.CustomNotNull;

public class Form {

	@Id
	private String formId;

	@CustomNotEmpty(message = "DAO_VARIABLE_REQUIRED", values = { "FORM_NAME" })
	private String name;

	private String description;

	private List<FormPanel> panels;

	@CustomNotNull(message = "NOT_NULL", values = { "LAYOUT_STYLE" })
	@Pattern(regexp = "fill|standard|outline", message = "INVALID_LAYOUT_STYLE")
	private String layoutStyle;

	private SaveButton saveButton;

	private Date dateCreated;

	private Date dateUpdated;

	private String createdBy;

	private String lastUpdatedBy;

	private String companyId;

	private String moduleId;

	private String displayImage;

	private List<String> visibleTo;

	private String workflow;

	public Form() {

	}

	public Form(String formId, String name, String description, List<FormPanel> panels,
			@Pattern(regexp = "fill|standard|outline", message = "INVALID_LAYOUT_STYLE") String layoutStyle,
			SaveButton saveButton, Date dateCreated, Date dateUpdated, String createdBy, String lastUpdatedBy,
			String companyId, String moduleId, String displayImage, List<String> visibleTo, String workflow) {
		super();
		this.formId = formId;
		this.name = name;
		this.description = description;
		this.panels = panels;
		this.layoutStyle = layoutStyle;
		this.saveButton = saveButton;
		this.dateCreated = dateCreated;
		this.dateUpdated = dateUpdated;
		this.createdBy = createdBy;
		this.lastUpdatedBy = lastUpdatedBy;
		this.companyId = companyId;
		this.moduleId = moduleId;
		this.displayImage = displayImage;
		this.visibleTo = visibleTo;
		this.workflow = workflow;
	}

	public String getFormId() {
		return formId;
	}

	public void setFormId(String formId) {
		this.formId = formId;
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

	public List<FormPanel> getPanels() {
		return panels;
	}

	public void setPanels(List<FormPanel> panels) {
		this.panels = panels;
	}

	public String getLayoutStyle() {
		return layoutStyle;
	}

	public void setLayoutStyle(String layoutStyle) {
		this.layoutStyle = layoutStyle;
	}

	public SaveButton getSaveButton() {
		return saveButton;
	}

	public void setSaveButton(SaveButton saveButton) {
		this.saveButton = saveButton;
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

	public String getModuleId() {
		return moduleId;
	}

	public void setModuleId(String moduleId) {
		this.moduleId = moduleId;
	}

	public String getDisplayImage() {
		return displayImage;
	}

	public void setDisplayImage(String displayImage) {
		this.displayImage = displayImage;
	}

	public List<String> getVisibleTo() {
		return visibleTo;
	}

	public void setVisibleTo(List<String> visibleTo) {
		this.visibleTo = visibleTo;
	}

	public String getWorkflow() {
		return workflow;
	}

	public void setWorkflow(String workflow) {
		this.workflow = workflow;
	}

}
