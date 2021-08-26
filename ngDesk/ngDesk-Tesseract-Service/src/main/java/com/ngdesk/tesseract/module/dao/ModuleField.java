package com.ngdesk.tesseract.module.dao;

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
	private Boolean notEditable;

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

	@JsonProperty("DATA_FILTER")
	@Field("DATA_FILTER")
	private DataFilter dataFilter;

	@JsonProperty("UNIQUE")
	@Field("UNIQUE")
	private Boolean unique;

	@Field("FORMULA")
	@JsonProperty("FORMULA")
	private String formula;

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

	public ModuleField() {

	}

	public ModuleField(String fieldId, String name, String displayLabel, DataType dataType, String module,
			String relationshipType, String relationshipField, String primaryDisplayField, Boolean required,
			Boolean visibility, List<String> picklistValues, String defaultValue, Boolean notEditable,
			Integer autoNumberStartingNumber, Map<String, String> inheritanceMapping, Boolean inheritedField,
			String inheritanceLevel, String aggregationField, String aggregationRelatedField, String aggregationType,
			DataFilter dataFilter, Boolean unique, String formula, String prefix, String suffix, String numericFormat,
			String toCurrency, String fromCurrency, String dateIncurred) {
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
		this.autoNumberStartingNumber = autoNumberStartingNumber;
		this.inheritanceMapping = inheritanceMapping;
		this.inheritedField = inheritedField;
		this.inheritanceLevel = inheritanceLevel;
		this.aggregationField = aggregationField;
		this.aggregationRelatedField = aggregationRelatedField;
		this.aggregationType = aggregationType;
		this.dataFilter = dataFilter;
		this.unique = unique;
		this.formula = formula;
		this.prefix = prefix;
		this.suffix = suffix;
		this.numericFormat = numericFormat;
		this.toCurrency = toCurrency;
		this.fromCurrency = fromCurrency;
		this.dateIncurred = dateIncurred;
	}

	public String getFieldId() {
		return fieldId;
	}

	public String getName() {
		return name;
	}

	public String getDisplayLabel() {
		return displayLabel;
	}

	public DataType getDataType() {
		return dataType;
	}

	public String getModule() {
		return module;
	}

	public String getRelationshipType() {
		return relationshipType;
	}

	public String getRelationshipField() {
		return relationshipField;
	}

	public String getPrimaryDisplayField() {
		return primaryDisplayField;
	}

	public Boolean getRequired() {
		return required;
	}

	public Boolean getVisibility() {
		return visibility;
	}

	public List<String> getPicklistValues() {
		return picklistValues;
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	public Boolean getNotEditable() {
		return notEditable;
	}

	public Integer getAutoNumberStartingNumber() {
		return autoNumberStartingNumber;
	}

	public Map<String, String> getInheritanceMapping() {
		return inheritanceMapping;
	}

	public Boolean getInheritedField() {
		return inheritedField;
	}

	public String getInheritanceLevel() {
		return inheritanceLevel;
	}

	public String getAggregationField() {
		return aggregationField;
	}

	public String getAggregationRelatedField() {
		return aggregationRelatedField;
	}

	public String getAggregationType() {
		return aggregationType;
	}

	public DataFilter getDataFilter() {
		return dataFilter;
	}

	public Boolean getUnique() {
		return unique;
	}

	public String getFormula() {
		return formula;
	}

	public String getPrefix() {
		return prefix;
	}

	public String getSuffix() {
		return suffix;
	}

	public String getNumericFormat() {
		return numericFormat;
	}

	public void setFieldId(String fieldId) {
		this.fieldId = fieldId;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setDisplayLabel(String displayLabel) {
		this.displayLabel = displayLabel;
	}

	public void setDataType(DataType dataType) {
		this.dataType = dataType;
	}

	public void setModule(String module) {
		this.module = module;
	}

	public void setRelationshipType(String relationshipType) {
		this.relationshipType = relationshipType;
	}

	public void setRelationshipField(String relationshipField) {
		this.relationshipField = relationshipField;
	}

	public void setPrimaryDisplayField(String primaryDisplayField) {
		this.primaryDisplayField = primaryDisplayField;
	}

	public void setRequired(Boolean required) {
		this.required = required;
	}

	public void setVisibility(Boolean visibility) {
		this.visibility = visibility;
	}

	public void setPicklistValues(List<String> picklistValues) {
		this.picklistValues = picklistValues;
	}

	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}

	public void setNotEditable(Boolean notEditable) {
		this.notEditable = notEditable;
	}

	public void setAutoNumberStartingNumber(Integer autoNumberStartingNumber) {
		this.autoNumberStartingNumber = autoNumberStartingNumber;
	}

	public void setInheritanceMapping(Map<String, String> inheritanceMapping) {
		this.inheritanceMapping = inheritanceMapping;
	}

	public void setInheritedField(Boolean inheritedField) {
		this.inheritedField = inheritedField;
	}

	public void setInheritanceLevel(String inheritanceLevel) {
		this.inheritanceLevel = inheritanceLevel;
	}

	public void setAggregationField(String aggregationField) {
		this.aggregationField = aggregationField;
	}

	public void setAggregationRelatedField(String aggregationRelatedField) {
		this.aggregationRelatedField = aggregationRelatedField;
	}

	public void setAggregationType(String aggregationType) {
		this.aggregationType = aggregationType;
	}

	public void setDataFilter(DataFilter dataFilter) {
		this.dataFilter = dataFilter;
	}

	public void setUnique(Boolean unique) {
		this.unique = unique;
	}

	public void setFormula(String formula) {
		this.formula = formula;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	public void setSuffix(String suffix) {
		this.suffix = suffix;
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

}
