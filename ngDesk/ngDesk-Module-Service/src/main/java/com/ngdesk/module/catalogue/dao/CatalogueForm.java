package com.ngdesk.module.catalogue.dao;

import com.ngdesk.commons.annotations.CustomNotEmpty;

import io.swagger.v3.oas.annotations.media.Schema;

public class CatalogueForm {

	@Schema(description = "form Ids")
	@CustomNotEmpty(message = "DAO_VARIABLE_REQUIRED", values = { "CATALOGUE_FORM" })
	private String formId;

	@Schema(description = "module Ids")
	@CustomNotEmpty(message = "DAO_VARIABLE_REQUIRED", values = { "CATALOGUE_MODULE" })
	private String moduleId;

	public CatalogueForm() {

	}

	public CatalogueForm(String formId, String moduleId) {
		super();
		this.formId = formId;
		this.moduleId = moduleId;
	}

	public String getFormId() {
		return formId;
	}

	public void setFormId(String formId) {
		this.formId = formId;
	}

	public String getModuleId() {
		return moduleId;
	}

	public void setModuleId(String moduleId) {
		this.moduleId = moduleId;
	}

}
