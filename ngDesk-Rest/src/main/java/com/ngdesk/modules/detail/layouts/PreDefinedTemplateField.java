package com.ngdesk.modules.detail.layouts;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PreDefinedTemplateField {

	@JsonProperty("FIELD_ID")
	@NotNull(message = "FIELD_ID_NOT_NULL")
	@NotBlank(message = "FIELD_ID_EMPTY")
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
