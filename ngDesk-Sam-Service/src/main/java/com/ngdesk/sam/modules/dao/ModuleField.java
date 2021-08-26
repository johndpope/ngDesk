package com.ngdesk.sam.modules.dao;

import java.util.List;

import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ModuleField {

	@Field("FIELD_ID")
	@JsonProperty("FIELD_ID")
	private String fieldId;

	@Field("NAME")
	@JsonProperty("NAME")
	private String name;

	@Field("DISPLAY_LABEL")
	@JsonProperty("DISPLAY_LABEL")
	private String displayLabel;

	@Field("HELP_TEXT")
	@JsonProperty("HELP_TEXT")
	private String helpText;

	@Field("DEFAULT_VALUE")
	@JsonProperty("DEFAULT_VALUE")
	private String defaultValue;

	@Field("RELATIONSHIP_TYPE")
	@JsonProperty("RELATIONSHIP_TYPE")
	private String relationshipType;

	@Field("PRIMARY_DISPLAY_FIELD")
	@JsonProperty("PRIMARY_DISPLAY_FIELD")
	private String primaryField;

	@Field("RELATIONSHIP_FIELD")
	@JsonProperty("RELATIONSHIP_FIELD")
	private String relationshipField;

	@Field("MODULE")
	@JsonProperty("MODULE")
	private String module;

	@JsonProperty("PREFIX")
	@Field("PREFIX")
	private String prefix;

	@JsonProperty("SUFFIX")
	@Field("SUFFIX")
	private String suffix;

	@JsonProperty("NUMERIC_FORMAT")
	@Field("NUMERIC_FORMAT")
	private String numericFormat;

	@Field("DATA_TYPE")
	private DataType dataType;

	@JsonProperty("CONDITIONS")
	@Field("CONDITIONS")
	List<Condition> conditions;

	ModuleField() {

	}

	public ModuleField(String fieldId, String name, String displayLabel, String helpText, String defaultValue,
			String relationshipType, String primaryField, String relationshipField, String module, String prefix,
			String suffix, String numericFormat, DataType dataType, List<Condition> conditions) {
		super();
		this.fieldId = fieldId;
		this.name = name;
		this.displayLabel = displayLabel;
		this.helpText = helpText;
		this.defaultValue = defaultValue;
		this.relationshipType = relationshipType;
		this.primaryField = primaryField;
		this.relationshipField = relationshipField;
		this.module = module;
		this.prefix = prefix;
		this.suffix = suffix;
		this.numericFormat = numericFormat;
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

	public String getHelpText() {
		return helpText;
	}

	public void setHelpText(String helpText) {
		this.helpText = helpText;
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}

	public String getRelationshipType() {
		return relationshipType;
	}

	public void setRelationshipType(String relationshipType) {
		this.relationshipType = relationshipType;
	}

	public String getPrimaryField() {
		return primaryField;
	}

	public void setPrimaryField(String primaryField) {
		this.primaryField = primaryField;
	}

	public String getRelationshipField() {
		return relationshipField;
	}

	public void setRelationshipField(String relationshipField) {
		this.relationshipField = relationshipField;
	}

	public String getModule() {
		return module;
	}

	public void setModule(String module) {
		this.module = module;
	}

	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	public String getSuffix() {
		return suffix;
	}

	public void setSuffix(String suffix) {
		this.suffix = suffix;
	}

	public String getNumericFormat() {
		return numericFormat;
	}

	public void setNumericFormat(String numericFormat) {
		this.numericFormat = numericFormat;
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
