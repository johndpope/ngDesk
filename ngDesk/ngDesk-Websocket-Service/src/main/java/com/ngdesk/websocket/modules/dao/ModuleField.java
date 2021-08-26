package com.ngdesk.websocket.modules.dao;

import java.util.List;
import java.util.Map;

import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

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

	@JsonProperty("MODULE")
	@Field("MODULE")
	private String module;

	@JsonProperty("RELATIONSHIP_TYPE")
	@Field("RELATIONSHIP_TYPE")
	private String relationshipType;

	@JsonProperty("RELATIONSHIP_FIELD")
	@Field("RELATIONSHIP_FIELD")
	private String relationshipField;

	@JsonProperty("PRIMARY_DISPLAY_FIELD")
	@Field("PRIMARY_DISPLAY_FIELD")
	private String primaryDisplayField;

	@JsonProperty("REQUIRED")
	@Field("REQUIRED")
	private Boolean required;

	@JsonProperty("VISIBILITY")
	@Field("VISIBILITY")
	private Boolean visibility;

	@JsonProperty("PICKLIST_VALUES")
	@Field("PICKLIST_VALUES")
	private List<String> picklistValues;

	@JsonProperty("DEFAULT_VALUE")
	@Field("DEFAULT_VALUE")
	private String defaultValue;

	@JsonProperty("NOT_EDITABLE")
	@Field("NOT_EDITABLE")
	private boolean notEditable;

	public ModuleField() {

	}

	public ModuleField(String fieldId, String name, String displayLabel, DataType dataType, String module,
			String relationshipType, String relationshipField, String primaryDisplayField, Boolean required,
			Boolean visibility, List<String> picklistValues, String defaultValue, boolean editable) {
		super();
		this.fieldId = fieldId;
		this.name = name;
		this.displayLabel = displayLabel;
		this.dataType = dataType;
		this.module = module;
		this.relationshipType = relationshipType;
		this.relationshipField = relationshipField;
		this.primaryDisplayField = primaryDisplayField;
		this.required = required;
		this.visibility = visibility;
		this.picklistValues = picklistValues;
		this.defaultValue = defaultValue;
		this.notEditable = notEditable;
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

	public String getModule() {
		return module;
	}

	public void setModule(String module) {
		this.module = module;
	}

	public String getRelationshipType() {
		return relationshipType;
	}

	public void setRelationshipType(String relationshipType) {
		this.relationshipType = relationshipType;
	}

	public String getRelationshipField() {
		return relationshipField;
	}

	public void setRelationshipField(String relationshipField) {
		this.relationshipField = relationshipField;
	}

	public String getPrimaryDisplayField() {
		return primaryDisplayField;
	}

	public void setPrimaryDisplayField(String primaryDisplayField) {
		this.primaryDisplayField = primaryDisplayField;
	}

	public Boolean getRequired() {
		return required;
	}

	public void setRequired(Boolean required) {
		this.required = required;
	}

	public Boolean getVisibility() {
		return visibility;
	}

	public void setVisibility(Boolean visibility) {
		this.visibility = visibility;
	}

	public List<String> getPicklistValues() {
		return picklistValues;
	}

	public void setPicklistValues(List<String> picklistValues) {
		this.picklistValues = picklistValues;
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}

	public boolean isNotEditable() {
		return notEditable;
	}

	public void setNotEditable(boolean notEditable) {
		this.notEditable = notEditable;
	}

}
