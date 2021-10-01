
package com.ngdesk.graphql.modules.dao;

import java.util.List;
import java.util.Map;

import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ModuleField {

	@Field("FIELD_ID")
	private String fieldId;

	@Field("NAME")
	private String name;

	@Field("DISPLAY_LABEL")
	private String displayLabel;

	@Field("HELP_TEXT")
	private String helpText;

	@Field("DEFAULT_VALUE")
	private String defaultValue;

	@Field("RELATIONSHIP_TYPE")
	private String relationshipType;

	@Field("PRIMARY_DISPLAY_FIELD")
	private String primaryDisplayField;

	@Field("RELATIONSHIP_FIELD")
	private String relationshipField;

	@Field("MODULE")
	private String module;

	@Field("DATA_TYPE")
	private DataType dataType;

	@Field("VISIBILITY")
	private Boolean visibility;

	@Field("REQUIRED")
	private Boolean required;

	@Field("INTERNAL")
	private Boolean internal;

	@Field("NOT_EDITABLE")
	private Boolean notEditable;

	@Field("MAX_SIZE")
	private Integer maxSize;

	@Field("AUTO_NUMBER_GENERATION")
	private Boolean autonumberGeneration;

	@Field("AUTO_NUMBER_STARTING_NUMBER")
	private Long autonumberStartingNumber;

	@Field("PICKLIST_DISPLAY_ALPHABETICALLY")
	private Boolean picklistDisplayAlphabetically;

	@Field("PICKLIST_USE_FIRST_VALUE")
	private Boolean picklistUseFirstValue;

	@Field("PICKLIST_VALUES")
	private List<String> picklistValues;

	@Field("GROUP_ID")
	private String groupId;

	@Field("DATA_FILTER")
	private DataFilter dataFilter;

	@Field("AGGREGATION_TYPE")
	private String aggregationType;

	@Field("AGGREGATION_FIELD")
	private String aggregationField;

	@Field("AGGREGATION_RELATED_FIELD")
	private String aggregationRelatedField;

	@Field("INHERITANCE_MAPPING")
	private Map<String, String> inheritanceMapping;

	@Field("FORMULA")
	private String formula;

	@Field("LIST_FORMULA")
	private List<ListFormulaField> listFormula;

	@Field("CREATED_BY")
	private String createdBy;

	@Field("LAST_UPDATED_BY")
	private String lastUpdatedBy;

	@Field("PREFIX")
	private String prefix;

	@Field("SUFFIX")
	private String suffix;

	@Field("NUMERIC_FORMAT")
	private String numericFormat;

	private Boolean inherited;

	@Field("TO_CURRENCY")
	private String toCurrency;

	@Field("FROM_CURRENCY")
	private String fromCurrency;

	@Field("DATE_INCURRED")
	private String dateIncurred;

	@Field("CONDITIONS")
	List<Condition> conditions;

	public ModuleField() {

	}

	public ModuleField(String fieldId, String name, String displayLabel, String helpText, String defaultValue,
			String relationshipType, String primaryDisplayField, String relationshipField, String module,
			DataType dataType, Boolean visibility, Boolean required, Boolean internal, Boolean notEditable,
			Integer maxSize, Boolean autonumberGeneration, Long autonumberStartingNumber,
			Boolean picklistDisplayAlphabetically, Boolean picklistUseFirstValue, List<String> picklistValues,
			String groupId, DataFilter dataFilter, String aggregationType, String aggregationField,
			String aggregationRelatedField, Map<String, String> inheritanceMapping, String formula,
			List<com.ngdesk.graphql.modules.dao.ListFormulaField> listFormula, String createdBy, String lastUpdatedBy,
			String prefix, String suffix, String numericFormat, Boolean inherited, String toCurrency,
			String fromCurrency, String dateIncurred, List<Condition> conditions) {
		super();
		this.fieldId = fieldId;
		this.name = name;
		this.displayLabel = displayLabel;
		this.helpText = helpText;
		this.defaultValue = defaultValue;
		this.relationshipType = relationshipType;
		this.primaryDisplayField = primaryDisplayField;
		this.relationshipField = relationshipField;
		this.module = module;
		this.dataType = dataType;
		this.visibility = visibility;
		this.required = required;
		this.internal = internal;
		this.notEditable = notEditable;
		this.maxSize = maxSize;
		this.autonumberGeneration = autonumberGeneration;
		this.autonumberStartingNumber = autonumberStartingNumber;
		this.picklistDisplayAlphabetically = picklistDisplayAlphabetically;
		this.picklistUseFirstValue = picklistUseFirstValue;
		this.picklistValues = picklistValues;
		this.groupId = groupId;
		this.dataFilter = dataFilter;
		this.aggregationType = aggregationType;
		this.aggregationField = aggregationField;
		this.aggregationRelatedField = aggregationRelatedField;
		this.inheritanceMapping = inheritanceMapping;
		this.formula = formula;
		this.listFormula = listFormula;
		this.createdBy = createdBy;
		this.lastUpdatedBy = lastUpdatedBy;
		this.prefix = prefix;
		this.suffix = suffix;
		this.numericFormat = numericFormat;
		this.inherited = inherited;
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

	public String getPrimaryDisplayField() {
		return primaryDisplayField;
	}

	public void setPrimaryDisplayField(String primaryDisplayField) {
		this.primaryDisplayField = primaryDisplayField;
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

	public DataType getDataType() {
		return dataType;
	}

	public void setDataType(DataType dataType) {
		this.dataType = dataType;
	}

	public Boolean getVisibility() {
		return visibility;
	}

	public void setVisibility(Boolean visibility) {
		this.visibility = visibility;
	}

	public Boolean getRequired() {
		return required;
	}

	public void setRequired(Boolean required) {
		this.required = required;
	}

	public Boolean getInternal() {
		return internal;
	}

	public void setInternal(Boolean internal) {
		this.internal = internal;
	}

	public Boolean getNotEditable() {
		return notEditable;
	}

	public void setNotEditable(Boolean notEditable) {
		this.notEditable = notEditable;
	}

	public Integer getMaxSize() {
		return maxSize;
	}

	public void setMaxSize(Integer maxSize) {
		this.maxSize = maxSize;
	}

	public Boolean getAutonumberGeneration() {
		return autonumberGeneration;
	}

	public void setAutonumberGeneration(Boolean autonumberGeneration) {
		this.autonumberGeneration = autonumberGeneration;
	}

	public Long getAutonumberStartingNumber() {
		return autonumberStartingNumber;
	}

	public void setAutonumberStartingNumber(Long autonumberStartingNumber) {
		this.autonumberStartingNumber = autonumberStartingNumber;
	}

	public Boolean getPicklistDisplayAlphabetically() {
		return picklistDisplayAlphabetically;
	}

	public void setPicklistDisplayAlphabetically(Boolean picklistDisplayAlphabetically) {
		this.picklistDisplayAlphabetically = picklistDisplayAlphabetically;
	}

	public Boolean getPicklistUseFirstValue() {
		return picklistUseFirstValue;
	}

	public void setPicklistUseFirstValue(Boolean picklistUseFirstValue) {
		this.picklistUseFirstValue = picklistUseFirstValue;
	}

	public List<String> getPicklistValues() {
		return picklistValues;
	}

	public void setPicklistValues(List<String> picklistValues) {
		this.picklistValues = picklistValues;
	}

	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	public DataFilter getDataFilter() {
		return dataFilter;
	}

	public void setDataFilter(DataFilter dataFilter) {
		this.dataFilter = dataFilter;
	}

	public String getAggregationType() {
		return aggregationType;
	}

	public void setAggregationType(String aggregationType) {
		this.aggregationType = aggregationType;
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

	public Map<String, String> getInheritanceMapping() {
		return inheritanceMapping;
	}

	public void setInheritanceMapping(Map<String, String> inheritanceMapping) {
		this.inheritanceMapping = inheritanceMapping;
	}

	public String getFormula() {
		return formula;
	}

	public void setFormula(String formula) {
		this.formula = formula;
	}

	public List<ListFormulaField> getListFormula() {
		return listFormula;
	}

	public void setListFormula(List<ListFormulaField> listFormula) {
		this.listFormula = listFormula;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public String getLastUpdatedBy() {
		return lastUpdatedBy;
	}

	public void setLastUpdatedBy(String lastUpdatedBy) {
		this.lastUpdatedBy = lastUpdatedBy;
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

	public Boolean getInherited() {
		return inherited;
	}

	public void setInherited(Boolean inherited) {
		this.inherited = inherited;
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