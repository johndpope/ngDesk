package com.ngdesk.module.layout.dao;

import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PreDefinedTemplateField {

	@JsonProperty("FIELD_ID")
	@Field("FIELD_ID")
	private String fieldId;

	public PreDefinedTemplateField() {
	}

	public PreDefinedTemplateField(String fieldId) {
		super();
		this.fieldId = fieldId;
	}

	public String getFieldId() {
		return fieldId;
	}

	public void setFieldId(String fieldId) {
		this.fieldId = fieldId;
	}

}
