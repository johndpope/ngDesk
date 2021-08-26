package com.ngdesk.company.module.dao;

import java.util.List;

import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PreDefinedTemplate {

	@JsonProperty("ID")
	@Field("ID")
	private String sectionId;

	@JsonProperty("FIELDS")
	@Field("FIELDS")
	private List<PreDefinedTemplateField> fields;

	@JsonProperty("FIELD_STYLE")
	@Field("FIELD_STYLE")
	private String fieldStyle;

	public PreDefinedTemplate() {
	}
	

	public PreDefinedTemplate(String sectionId, List<PreDefinedTemplateField> fields, String fieldStyle) {
		super();
		this.sectionId = sectionId;
		this.fields = fields;
		this.fieldStyle = fieldStyle;
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

}
