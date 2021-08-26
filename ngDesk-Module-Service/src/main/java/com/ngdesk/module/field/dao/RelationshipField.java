package com.ngdesk.module.field.dao;

import javax.validation.Valid;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ngdesk.commons.annotations.CustomNotNull;

public class RelationshipField {

	@JsonProperty("FIELD")
	@CustomNotNull(message = "DAO_VARIABLE_REQUIRED", values = { "FIELD" })
	@Valid
	private ModuleField field;

	@JsonProperty("RELATED_FIELD")
	@CustomNotNull(message = "DAO_VARIABLE_REQUIRED", values = { "RELATED_FIELD" })
	@Valid
	private ModuleField relatedField;

	public RelationshipField() {

	}

	public RelationshipField(ModuleField field, ModuleField relatedField) {
		super();
		this.field = field;
		this.relatedField = relatedField;
	}

	public ModuleField getField() {
		return field;
	}

	public void setField(ModuleField field) {
		this.field = field;
	}

	public ModuleField getRelatedField() {
		return relatedField;
	}

	public void setRelatedField(ModuleField relatedField) {
		this.relatedField = relatedField;
	}

}
