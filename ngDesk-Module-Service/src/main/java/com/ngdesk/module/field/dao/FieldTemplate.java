package com.ngdesk.module.field.dao;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;

public class FieldTemplate {

	@JsonProperty("FIELDS")
	@Field("FIELDS")
	private List<ModuleField> fields = new ArrayList<ModuleField>();

	@JsonProperty("TIER")
	@Field("TIER")
	private String tier = "free";

	@JsonProperty("MODULE_ID")
	@Field("MODULE_ID")
	private String moduleId;

	public FieldTemplate() {
	}

	public FieldTemplate(List<ModuleField> fields, String tier, String moduleId) {
		super();
		this.fields = fields;
		this.tier = tier;
		this.moduleId = moduleId;
	}

	public List<ModuleField> getFields() {
		return fields;
	}

	public void setFields(List<ModuleField> fields) {
		this.fields = fields;
	}

	public String getTier() {
		return tier;
	}

	public void setTier(String tier) {
		this.tier = tier;
	}

	public String getModuleId() {
		return moduleId;
	}

	public void setModuleId(String moduleId) {
		this.moduleId = moduleId;
	}

}
