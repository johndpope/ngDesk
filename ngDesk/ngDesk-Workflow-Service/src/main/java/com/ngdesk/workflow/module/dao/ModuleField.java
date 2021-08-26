package com.ngdesk.workflow.module.dao;

import java.util.List;
import java.util.Map;

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
	private Boolean editable;

	@JsonProperty("AUTO_NUMBER_STARTING_NUMBER")
	@Field("AUTO_NUMBER_STARTING_NUMBER")
	private Integer autoNumberStartingNumber;

	@JsonProperty("INHERITANCE_MAPPING")
	@Field("INHERITANCE_MAPPING")
	private Map<String, String> inheritanceMapping;

	@JsonProperty("INHERITED_FIELD")
	private Boolean inheritedField;

	@JsonProperty("INHERITANCE_LEVEL")
	private String inheritanceLevel;

	@JsonProperty("AGGREGATION_FIELD")
	@Field("AGGREGATION_FIELD")
	private String aggregationField;

	@JsonProperty("AGGREGATION_RELATED_FIELD")
	@Field("AGGREGATION_RELATED_FIELD")
	private String aggregationRelatedField;

	@JsonProperty("AGGREGATION_TYPE")
	@Field("AGGREGATION_TYPE")
	private String aggregationType;

	@JsonProperty("IS_LIST_TEXT_UNIQUE")
	@Field("IS_LIST_TEXT_UNIQUE")
	private Boolean listTextUnique;

	@JsonProperty("PREFIX")
	@Field("PREFIX")
	private String prefix;

	@JsonProperty("SUFFIX")
	@Field("SUFFIX")
	private String suffix;

	@JsonProperty("NUMERIC_FORMAT")
	@Field("NUMERIC_FORMAT")
	private String numericFormat;

	@JsonProperty("TO_CURRENCY")
	@Field("TO_CURRENCY")
	private String toCurrency;

	@JsonProperty("FROM_CURRENCY")
	@Field("FROM_CURRENCY")
	private String fromCurrency;

	@JsonProperty("DATE_INCURRED")
	@Field("DATE_INCURRED")
	private String dateIncurred;

	@JsonProperty("CONDITIONS")
	@Field("CONDITIONS")
	List<Condition> conditions;

	public ModuleField() {

	}

	public ModuleField(String fieldId, String name, String displayLabel, DataType dataType, String module,
			String relationshipType, String relationshipField, String primaryDisplayField, Boolean required,
			Boolean visibility, List<String> picklistValues, String defaultValue, Boolean editable,
			Integer autoNumberStartingNumber, Map<String, String> inheritanceMapping, Boolean inheritedField,
			String inheritanceLevel, String aggregationField, String aggregationRelatedField, String aggregationType,
			Boolean listTextUnique, String prefix, String suffix, String numericFormat, String toCurrency,
			String fromCurrency, String dateIncurred, List<Condition> conditions) {
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
		this.editable = editable;
		this.autoNumberStartingNumber = autoNumberStartingNumber;
		this.inheritanceMapping = inheritanceMapping;
		this.inheritedField = inheritedField;
		this.inheritanceLevel = inheritanceLevel;
		this.aggregationField = aggregationField;
		this.aggregationRelatedField = aggregationRelatedField;
		this.aggregationType = aggregationType;
		this.listTextUnique = listTextUnique;
		this.prefix = prefix;
		this.suffix = suffix;
		this.numericFormat = numericFormat;
		this.toCurrency = toCurrency;
		this.fromCurrency = fromCurrency;
		this.dateIncurred = dateIncurred;
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

	public Boolean getEditable() {
		return editable;
	}

	public void setEditable(Boolean editable) {
		this.editable = editable;
	}

	public Integer getAutoNumberStartingNumber() {
		return autoNumberStartingNumber;
	}

	public void setAutoNumberStartingNumber(Integer autoNumberStartingNumber) {
		this.autoNumberStartingNumber = autoNumberStartingNumber;
	}

	public Map<String, String> getInheritanceMapping() {
		return inheritanceMapping;
	}

	public void setInheritanceMapping(Map<String, String> inheritanceMapping) {
		this.inheritanceMapping = inheritanceMapping;
	}

	public Boolean getInheritedField() {
		return inheritedField;
	}

	public void setInheritedField(Boolean inheritedField) {
		this.inheritedField = inheritedField;
	}

	public String getInheritanceLevel() {
		return inheritanceLevel;
	}

	public void setInheritanceLevel(String inheritanceLevel) {
		this.inheritanceLevel = inheritanceLevel;
	}

	public String getAggregationField() {
		return aggregationField;
	}

	public void setAggregationField(String aggregationField) {
		this.aggregationField = aggregationField;
	}

	public String getAggregationRelatedField() {
		return aggregationRelatedField;
	}

	public void setAggregationRelatedField(String aggregationRelatedField) {
		this.aggregationRelatedField = aggregationRelatedField;
	}

	public String getAggregationType() {
		return aggregationType;
	}

	public void setAggregationType(String aggregationType) {
		this.aggregationType = aggregationType;
	}

	public Boolean getListTextUnique() {
		return listTextUnique;
	}

	public void setListTextUnique(Boolean listTextUnique) {
		this.listTextUnique = listTextUnique;
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

	public String getToCurrency() {
		return toCurrency;
	}

	public void setToCurrency(String toCurrency) {
		this.toCurrency = toCurrency;
	}

	public String getFromCurrency() {
		return fromCurrency;
	}

	public void setFromCurrency(String fromCurrency) {
		this.fromCurrency = fromCurrency;
	}

	public String getDateIncurred() {
		return dateIncurred;
	}

	public void setDateIncurred(String dateIncurred) {
		this.dateIncurred = dateIncurred;
	}

	public List<Condition> getConditions() {
		return conditions;
	}

	public void setConditions(List<Condition> conditions) {
		this.conditions = conditions;
	}

}
