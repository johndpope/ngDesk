package com.ngdesk.report.module.dao;

import java.util.List;
import java.util.Map;

import org.springframework.data.mongodb.core.mapping.Field;

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

	@Field("PICKLIST_VALUES")
	private List<String> picklistValues;

	@Field("GROUP_ID")
	private String groupId;

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

	@Field("CREATED_BY")
	private String createdBy;

	@Field("LAST_UPDATED_BY")
	private String lastUpdatedBy;

	@Field("CONDITIONS")
	List<Condition> conditions;

	public ModuleField() {

	}

	public ModuleField(String fieldId, String name, String displayLabel, String helpText, String defaultValue,
			String relationshipType, String primaryDisplayField, String relationshipField, String module,
			DataType dataType, Boolean visibility, Boolean required, Boolean internal, Boolean notEditable,
			Integer maxSize, List<String> picklistValues, String groupId, String aggregationType,
			String aggregationField, String aggregationRelatedField, Map<String, String> inheritanceMapping,
			String formula, String createdBy, String lastUpdatedBy, List<Condition> conditions) {
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
		this.picklistValues = picklistValues;
		this.groupId = groupId;
		this.aggregationType = aggregationType;
		this.aggregationField = aggregationField;
		this.aggregationRelatedField = aggregationRelatedField;
		this.inheritanceMapping = inheritanceMapping;
		this.formula = formula;
		this.createdBy = createdBy;
		this.lastUpdatedBy = lastUpdatedBy;
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

	public List<Condition> getConditions() {
		return conditions;
	}

	public void setConditions(List<Condition> conditions) {
		this.conditions = conditions;
	}

}
