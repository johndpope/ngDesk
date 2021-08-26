package com.ngdesk.modules.forms;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ngdesk.company.module.dao.Grid;

public class Form {

	@JsonProperty("NAME")
	@NotNull(message = "FORM_NAME_NOT_NULL")
	@NotBlank(message = "FORM_NAME_NOT_BLANK")
	@Field("NAME")
	private String name;

	@JsonProperty("FORM_ID")
	@Field("FORM_ID")
	private String formId;

	@JsonProperty("DESCRIPTION")
	@Field("DESCRIPTION")
	private String description;

	@JsonProperty("GRIDS")
	@Field("GRIDS")
	private List<List<Grid>> grids;

	@JsonProperty("LAYOUT_STYLE")
	@Field("LAYOUT_STYLE")
	@Pattern(regexp = "fill|standard|outline", message = "INVALID_LAYOUT_STYLE")
	private String layoutStyle;

	@JsonProperty("SAVE_BUTTON")
	@Field("SAVE_BUTTON")
	@Valid
	private SaveButton saveButton;

	public Form() {

	}

	public Form(@NotNull(message = "FORM_NAME_NOT_NULL") @NotBlank(message = "FORM_NAME_NOT_BLANK") String name,
			String formId, String description, List<List<Grid>> grids,
			@Pattern(regexp = "fill|standard|outline", message = "INVALID_LAYOUT_STYLE") String layoutStyle,
			@Valid SaveButton saveButton) {
		super();
		this.name = name;
		this.formId = formId;
		this.description = description;
		this.grids = grids;
		this.layoutStyle = layoutStyle;
		this.saveButton = saveButton;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getFormId() {
		return formId;
	}

	public void setFormId(String formId) {
		this.formId = formId;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public List<List<Grid>> getGrids() {
		return grids;
	}

	public void setGrids(List<List<Grid>> grids) {
		this.grids = grids;
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

}
