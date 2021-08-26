package com.ngdesk.module.layout.dao;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ngdesk.commons.annotations.CustomNotEmpty;
import com.ngdesk.commons.annotations.CustomNotNull;

public class CreateEditLayout {

	@Field("LAYOUT_ID")
	@JsonProperty("LAYOUT_ID")
	private String layoutId = UUID.randomUUID().toString();

	@Field("NAME")
	@JsonProperty("NAME")
	@CustomNotEmpty(message = "DAO_VARIABLE_REQUIRED", values = { "LAYOUT_NAME" })
	@CustomNotNull(message = "NOT_NULL", values = { "LAYOUT_NAME" })
	private String name;

	@Field("DESCRIPTION")
	@JsonProperty("DESCRIPTION")
	@CustomNotNull(message = "NOT_NULL", values = { "DESCRIPTION" })
	private String description;

	@Field("PANELS")
	@JsonProperty("PANELS")
	private List<Panel> panels;

	@Field("TITLE_BAR")
	@JsonProperty("TITLE_BAR")
	private List<TitleBarGrid> titleBar;

	@Field("PREDEFINED_TEMPLATE")
	@JsonProperty("PREDEFINED_TEMPLATE")
	private List<PreDefinedTemplate> preDefinedTemplate;

	@Field("CUSTOM_LAYOUT")
	@JsonProperty("CUSTOM_LAYOUT")
	private String customLayout;

	@Field("ROLE")
	@JsonProperty("ROLE")
	@CustomNotNull(message = "NOT_NULL", values = { "ROLE" })
	private String role;

	// TODO: Validation required
	@Field("LAYOUT_STYLE")
	@JsonProperty("LAYOUT_STYLE")
	@CustomNotNull(message = "DAO_VARIABLE_REQUIRED", values = { "CREATE_LAYOUT_STYLE" })
	private String layoutStyle;

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

	public CreateEditLayout() {
		super();
	}

	public CreateEditLayout(String layoutId, String name, String description, List<Panel> panels,
			List<TitleBarGrid> titleBar, List<PreDefinedTemplate> preDefinedTemplate, String customLayout, String role,
			String layoutStyle, Date dateCreated, Date dateUpdated, String createdBy, String lastUpdatedBy) {
		super();
		this.layoutId = layoutId;
		this.name = name;
		this.description = description;
		this.panels = panels;
		this.titleBar = titleBar;
		this.preDefinedTemplate = preDefinedTemplate;
		this.customLayout = customLayout;
		this.role = role;
		this.layoutStyle = layoutStyle;
		this.dateCreated = dateCreated;
		this.dateUpdated = dateUpdated;
		this.createdBy = createdBy;
		this.lastUpdatedBy = lastUpdatedBy;
	}

	public String getLayoutId() {
		return layoutId;
	}

	public void setLayoutId(String layoutId) {
		this.layoutId = layoutId;
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

	public List<Panel> getPanels() {
		return panels;
	}

	public void setPanels(List<Panel> panels) {
		this.panels = panels;
	}

	public List<TitleBarGrid> getTitleBar() {
		return titleBar;
	}

	public void setTitleBar(List<TitleBarGrid> titleBar) {
		this.titleBar = titleBar;
	}

	public List<PreDefinedTemplate> getPreDefinedTemplate() {
		return preDefinedTemplate;
	}

	public void setPreDefinedTemplate(List<PreDefinedTemplate> preDefinedTemplate) {
		this.preDefinedTemplate = preDefinedTemplate;
	}

	public String getCustomLayout() {
		return customLayout;
	}

	public void setCustomLayout(String customLayout) {
		this.customLayout = customLayout;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public String getLayoutStyle() {
		return layoutStyle;
	}

	public void setLayoutStyle(String layoutStyle) {
		this.layoutStyle = layoutStyle;
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
