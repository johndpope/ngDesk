package com.ngdesk.graphql.catalogue.dao;

public class CatalogueForm {

	private String moduleId;
	private String formId;

	public CatalogueForm() {
		super();
	}

	public CatalogueForm(String moduleId, String formId) {
		super();
		this.moduleId = moduleId;
		this.formId = formId;
	}

	public String getModuleId() {
		return moduleId;
	}

	public void setModuleId(String moduleId) {
		this.moduleId = moduleId;
	}

	public String getFormId() {
		return formId;
	}

	public void setFormId(String formId) {
		this.formId = formId;
	}

}
