package com.ngdesk.modules.detail.layouts;

import java.util.Date;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties("_class")
public class DCELayout {

	@JsonProperty("LAYOUT_ID")
	private String layoutId;

	@JsonProperty("NAME")
	@NotNull(message = "LAYOUT_NAME_NOT_NULL")
	@Size(min = 1, message = "LAYOUT_NAME_NOT_EMPTY")
	private String name;

	@JsonProperty("DESCRIPTION")
	@NotNull(message = "DESCRIPTION_NOT_NULL")
	private String description;

	@JsonProperty("PANELS")
	@Valid
	private List<Panel> panels;

	@JsonProperty("TITLE_BAR")
	@Valid
	private List<TitleBarGrid> titleBar;

	@JsonProperty("PREDEFINED_TEMPLATE")
	@Valid
	private List<PreDefinedTemplate> preDefinedTemplate;

	@JsonProperty("CUSTOM_LAYOUT")
	private String customLayout;

	@JsonProperty("ROLE")
	@NotNull(message = "ROLE_NOT_NULL")
	private String role;

	@JsonProperty("DATE_CREATED")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
	private Date dateCreated;

	@JsonProperty("DATE_UPDATED")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
	private Date dateUpdated;

	@JsonProperty("LAST_UPDATED_BY")
	private String lastUpdatedBy;

	@JsonProperty("CREATED_BY")
	private String createdBy;

	@JsonProperty("LAYOUT_STYLE")
	@Pattern(regexp = "^(fill|standard|outline)$", message = "INVALID_LAYOUT_STYLE")
	private String layoutStyle;

	public DCELayout() {
	}

	public DCELayout(String layoutId,
			@NotNull(message = "LAYOUT_NAME_NOT_NULL") @Size(min = 1, message = "LAYOUT_NAME_NOT_EMPTY") String name,
			@NotNull(message = "DESCRIPTION_NOT_NULL") String description, @Valid List<Panel> panels,
			@Valid List<TitleBarGrid> titleBar, @Valid List<PreDefinedTemplate> preDefinedTemplate, String customLayout,
			@NotNull(message = "ROLE_NOT_NULL") String role, Date dateCreated, Date dateUpdated, String lastUpdatedBy,
			String createdBy,
			@Pattern(regexp = "^(fill|standard|outline)$", message = "INVALID_LAYOUT_STYLE") String layoutStyle) {
		super();
		this.layoutId = layoutId;
		this.name = name;
		this.description = description;
		this.panels = panels;
		this.titleBar = titleBar;
		this.preDefinedTemplate = preDefinedTemplate;
		this.customLayout = customLayout;
		this.role = role;
		this.dateCreated = dateCreated;
		this.dateUpdated = dateUpdated;
		this.lastUpdatedBy = lastUpdatedBy;
		this.createdBy = createdBy;
		this.layoutStyle = layoutStyle;
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

	public String getLayoutStyle() {
		return layoutStyle;
	}

	public void setLayoutStyle(String layoutStyle) {
		this.layoutStyle = layoutStyle;
	}

}
