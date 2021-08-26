package com.ngdesk.sam.modules.dao;

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

	public Module() {

	}

	public Module(String moduleId, String name, String description, String parentModule, String singularName,
			String pluralName, List<com.ngdesk.sam.modules.dao.ModuleField> fields) {
		super();
		this.moduleId = moduleId;
		this.name = name;
		this.description = description;
		this.parentModule = parentModule;
		this.singularName = singularName;
		this.pluralName = pluralName;
		this.fields = fields;
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

}
