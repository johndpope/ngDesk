package com.ngdesk.integration.module.dao;

import java.util.List;

import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ModuleField {
	@JsonProperty("FIELD_ID")
	@Field("FIELD_ID")
	private String fieldId;

	@JsonProperty("NAME")
	@Field("NAME")
	private String name;

	@JsonProperty("DISPLAY_LABEL")
	@Field("DISPLAY_LABEL")
	private String displayLabel;

	@JsonProperty("DATA_TYPE")
	@Field("DATA_TYPE")
	private DataType dataType;

	@JsonProperty("CONDITIONS")
	@Field("CONDITIONS")
	List<Condition> conditions;

	public ModuleField(String fieldId, String name, String displayLabel, DataType dataType,
			List<Condition> conditions) {
		super();
		this.fieldId = fieldId;
		this.name = name;
		this.displayLabel = displayLabel;
		this.dataType = dataType;
		this.conditions = conditions;
	}

	public String getFieldId() {
		return fieldId;
	}

	public void setFieldId(String fieldId) {
		this.fieldId = fieldId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDisplayLabel() {
		return displayLabel;
	}

	public void setDisplayLabel(String displayLabel) {
		this.displayLabel = displayLabel;
	}

	public DataType getDataType() {
		return dataType;
	}

	public void setDataType(DataType dataType) {
		this.dataType = dataType;
	}

	public List<Condition> getConditions() {
		return conditions;
	}

	public void setConditions(List<Condition> conditions) {
		this.conditions = conditions;
	}

}
