package com.ngdesk.modules.detail.layouts;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PreDefinedTemplate {

	@JsonProperty("ID")
	@NotBlank(message = "SECTION_ID_EMPTY")
	private String sectionId;

	@JsonProperty("FIELDS")
	@Valid
	private List<PreDefinedTemplateField> fields;

	@JsonProperty("FIELD_STYLE")
	@Valid
	private String fieldStyle;

	public PreDefinedTemplate() {
	}

	public String getSectionId() {
		return sectionId;
	}

	public void setSectionId(String sectionId) {
		this.sectionId = sectionId;
	}

	public List<PreDefinedTemplateField> getFields() {
		return fields;
	}

	public void setFields(List<PreDefinedTemplateField> fields) {
		this.fields = fields;
	}

	public String getFieldStyle() {
		return fieldStyle;
	}

	public void setFieldStyle(String fieldStyle) {
		this.fieldStyle = fieldStyle;
	}

	public PreDefinedTemplate(@NotBlank(message = "SECTION_ID_EMPTY") String sectionId,
			@Valid List<PreDefinedTemplateField> fields, @Valid String fieldStyle) {
		super();
		this.sectionId = sectionId;
		this.fields = fields;
		this.fieldStyle = fieldStyle;
	}

}
