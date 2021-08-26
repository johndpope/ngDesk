package com.ngdesk.tesseract.module.dao;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Module {

	@Id
	@JsonProperty("MODULE_ID")
	private String moduleId;

	@JsonProperty("NAME")
	@Field("NAME")
	private String name;

	@JsonProperty("DESCRIPTION")
	@Field("DESCRIPTION")
	private String description;

	@JsonProperty("PARENT_MODULE")
	@Field("PARENT_MODULE")
	private String parentModule;

	@JsonProperty("SINGULAR_NAME")
	@Field("SINGULAR_NAME")
	private String singularName;

	@JsonProperty("PLURAL_NAME")
	@Field("PLURAL_NAME")
	private String pluralName;

	@JsonProperty("FIELDS")
	@Field("FIELDS")
	private List<ModuleField> fields;

	@JsonProperty("VALIDATIONS")
	@Field("VALIDATIONS")
	private List<ModuleValidation> validations;

	@JsonProperty("LIST_LAYOUTS")
	@Field("LIST_LAYOUTS")
	private List<ListLayout> listLayouts;

	@JsonProperty("LIST_MOBILE_LAYOUTS")
	@Field("LIST_MOBILE_LAYOUTS")
	private List<ListMobileLayout> listMobileLayouts;

	@JsonProperty("SLAS")
	@Field("SLAS")
	private List<Sla> slas;

	@JsonProperty("ALTERNATE_PRIMARY_KEYS")
	@Field("ALTERNATE_PRIMARY_KEYS")
	private List<String> alternatePrimaryKeys;

	public Module() {

	}

	public Module(String moduleId, String name, String description, String parentModule, String singularName,
			String pluralName, List<ModuleField> fields, List<ModuleValidation> validations,
			List<ListLayout> listLayouts, List<ListMobileLayout> listMobileLayouts, List<Sla> slas,
			List<String> alternatePrimaryKeys) {
		super();
		this.moduleId = moduleId;
		this.name = name;
		this.description = description;
		this.parentModule = parentModule;
		this.singularName = singularName;
		this.pluralName = pluralName;
		this.fields = fields;
		this.validations = validations;
		this.listLayouts = listLayouts;
		this.listMobileLayouts = listMobileLayouts;
		this.slas = slas;
		this.alternatePrimaryKeys = alternatePrimaryKeys;
	}

	public String getModuleId() {
		return moduleId;
	}

	public void setModuleId(String moduleId) {
		this.moduleId = moduleId;
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

	public String getParentModule() {
		return parentModule;
	}

	public void setParentModule(String parentModule) {
		this.parentModule = parentModule;
	}

	public String getSingularName() {
		return singularName;
	}

	public void setSingularName(String singularName) {
		this.singularName = singularName;
	}

	public String getPluralName() {
		return pluralName;
	}

	public void setPluralName(String pluralName) {
		this.pluralName = pluralName;
	}

	public List<ModuleField> getFields() {
		return fields;
	}

	public void setFields(List<ModuleField> fields) {
		this.fields = fields;
	}

	public List<ModuleValidation> getValidations() {
		return validations;
	}

	public void setValidations(List<ModuleValidation> validations) {
		this.validations = validations;
	}

	public List<ListLayout> getListLayouts() {
		return listLayouts;
	}

	public void setListLayouts(List<ListLayout> listLayouts) {
		this.listLayouts = listLayouts;
	}

	public List<ListMobileLayout> getListMobileLayouts() {
		return listMobileLayouts;
	}

	public void setListMobileLayouts(List<ListMobileLayout> listMobileLayouts) {
		this.listMobileLayouts = listMobileLayouts;
	}

	public List<Sla> getSlas() {
		return slas;
	}

	public void setSlas(List<Sla> slas) {
		this.slas = slas;
	}

	public List<String> getAlternatePrimaryKeys() {
		return alternatePrimaryKeys;
	}

	public void setAlternatePrimaryKeys(List<String> alternatePrimaryKeys) {
		this.alternatePrimaryKeys = alternatePrimaryKeys;
	}

}
