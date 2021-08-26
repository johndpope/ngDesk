package com.ngdesk.integration.module.dao;

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

	@JsonProperty("FIELDS")
	@Field("FIELDS")
	private List<ModuleField> fields;

	public Module() {

	}

	public Module(String moduleId, String name, String description, String parentModule, List<ModuleField> fields) {
		super();
		this.moduleId = moduleId;
		this.name = name;
		this.description = description;
		this.parentModule = parentModule;
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

	public List<ModuleField> getFields() {
		return fields;
	}

	public void setFields(List<ModuleField> fields) {
		this.fields = fields;
	}

}
