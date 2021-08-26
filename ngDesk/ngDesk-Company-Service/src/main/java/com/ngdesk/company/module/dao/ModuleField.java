package com.ngdesk.company.module.dao;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.validation.constraints.Pattern;

import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ngdesk.commons.annotations.CustomNotEmpty;
import com.ngdesk.commons.annotations.CustomNotNull;

import io.swagger.v3.oas.annotations.media.Schema;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
public class ModuleField {

	@Field("FIELD_ID")
	@JsonProperty("FIELD_ID")
	String fieldId = UUID.randomUUID().toString();

	@Schema(description = "Name of the field", required = true)
	@Field("NAME")
	@JsonProperty("NAME")
	@CustomNotEmpty(message = "DAO_VARIABLE_REQUIRED", values = { "FIELD_NAME" })
	@CustomNotNull(message = "NOT_NULL", values = { "FIELD_NAME" })
	String name;

	@Schema(description = "Display label of the field", required = true)
	@Field("DISPLAY_LABEL")
	@JsonProperty("DISPLAY_LABEL")
	@CustomNotEmpty(message = "DAO_VARIABLE_REQUIRED", values = { "FIELD_DISPLAY_LABEL" })
	@CustomNotNull(message = "NOT_NULL", values = { "FIELD_DISPLAY_LABEL" })
	String displayLabel;

	@Schema(description = "Help text to be displayed", required = false)
	@Field("HELP_TEXT")
	@JsonProperty("HELP_TEXT")
	String helpText;

	@Schema(description = "Default value of the field", required = false)
	@Field("DEFAULT_VALUE")
	@JsonProperty("DEFAULT_VALUE")
	String defaultValue;

	@Schema(description = "Type of relation of field with other module", required = false, example = "Many to One")
	@Field("RELATIONSHIP_TYPE")
	@JsonProperty("RELATIONSHIP_TYPE")
	String relationshipType;

	@Schema(description = "Field of this module to be shown in UI", required = false)
	@Field("PRIMARY_DISPLAY_FIELD")
	@JsonProperty("PRIMARY_DISPLAY_FIELD")
	String primaryField;

	@Schema(description = "Reference to foreign field in the related module", required = false)
	@Field("RELATIONSHIP_FIELD")
	@JsonProperty("RELATIONSHIP_FIELD")
	String relationshipField;

	@Schema(description = "Related module's id for the field", required = false)
	@Field("MODULE")
	@JsonProperty("MODULE")
	String module;

	@Schema(description = "Data type of the field", required = true)
	@Field("DATA_TYPE")
	@JsonProperty("DATA_TYPE")
	DataType dataType;

	@Schema(description = "Whether the field will be visible in UI", required = true)
	@Field("VISIBILITY")
	@JsonProperty("VISIBILITY")
	Boolean visibility;

	@Schema(description = "Whether the field is mandatory", required = true)
	@Field("REQUIRED")
	@JsonProperty("REQUIRED")
	Boolean required;

	@Schema(description = "Whether the field is for internal purpose", required = true)
	@Field("INTERNAL")
	@JsonProperty("INTERNAL")
	Boolean internal;

	@Schema(description = "Whether the field is editable", required = true)
	@Field("NOT_EDITABLE")
	@JsonProperty("NOT_EDITABLE")
	Boolean notEditable;

	@Field("MAX_SIZE")
	@JsonProperty("MAX_SIZE")
	private int maxSize;

	@Field("AUTO_NUMBER_GENERATION")
	@JsonProperty("AUTO_NUMBER_GENERATION")
	private Boolean autonumberGeneration;

	@Field("AUTO_NUMBER_STARTING_NUMBER")
	@JsonProperty("AUTO_NUMBER_STARTING_NUMBER")
	private int autonumberStartingNumber;

	@Field("PICKLIST_DISPLAY_ALPHABETICALLY")
	@JsonProperty("PICKLIST_DISPLAY_ALPHABETICALLY")
	private Boolean picklistDisplay;

	@Field("PICKLIST_USE_FIRST_VALUE")
	@JsonProperty("PICKLIST_USE_FIRST_VALUE")
	private Boolean picklistFirstValue;

	@Field("PICKLIST_VALUES")
	@JsonProperty("PICKLIST_VALUES")
	private List<String> picklistValues;

	@Field("FORMULA")
	@JsonProperty("FORMULA")
	private String formula;

	@Field("MESSAGES")
	@JsonProperty("MESSAGES")
	private List<DiscussionMessage> messages;

	@Field("GROUP_ID")
	@JsonProperty("GROUP_ID")
	private String groupId;

	@Field("DATA_FILTER")
	@JsonProperty("DATA_FILTER")
	private DataFilter dataFilter;

	@Field("AGGREGATION_TYPE")
	@JsonProperty("AGGREGATION_TYPE")
	@Pattern(regexp = "sum", message = "NOT_VALID_AGGREGATION_TYPE")
	private String aggregationType;

	@Field("AGGREGATION_FIELD")
	@JsonProperty("AGGREGATION_FIELD")
	private String aggregationField;

	@Field("AGGREGATION_RELATED_FIELD")
	@JsonProperty("AGGREGATION_RELATED_FIELD")
	private String aggregationRelatedField;

	@Field("INHERITANCE_MAPPING")
	@JsonProperty("INHERITANCE_MAPPING")
	private Map<String, String> inheritanceMapping;

	@JsonProperty("DATE_CREATED")
	@Field("DATE_CREATED")
	private Date dateCreated = new Date();

	@JsonProperty("DATE_UPDATED")
	@Field("DATE_UPDATED")
	private Date dateUpdated = new Date();

	@JsonProperty("CREATED_BY")
	@Field("CREATED_BY")
	private String createdBy;

	@JsonProperty("LAST_UPDATED_BY")
	@Field("LAST_UPDATED_BY")
	private String lastUpdatedBy;

	@JsonProperty("WORKFLOW")
	@Field("WORKFLOW")
	private String workflow;

	@JsonProperty("PREFIX")
	@Field("PREFIX")
	private String prefix;

	@JsonProperty("SUFFIX")
	@Field("SUFFIX")
	private String suffix;

	@JsonProperty("NUMERIC_FORMAT")
	@Field("NUMERIC_FORMAT")
	private String numericFormat;

	@Schema(description = "Field mapping is the copy feature for module field", required = false)
	@Field("FIELDS_MAPPING")
	@JsonProperty("FIELDS_MAPPING")
	private Map<String, String> fieldsMapping;

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
		super();
	}

	public ModuleField(String fieldId, String name, String displayLabel, String helpText, String defaultValue,
			String relationshipType, String primaryField, String relationshipField, String module, DataType dataType,
			Boolean visibility, Boolean required, Boolean internal, Boolean notEditable, int maxSize,
			Boolean autonumberGeneration, int autonumberStartingNumber, Boolean picklistDisplay,
			Boolean picklistFirstValue, List<String> picklistValues, String formula, List<DiscussionMessage> messages,
			String groupId, DataFilter dataFilter,
			@Pattern(regexp = "sum", message = "NOT_VALID_AGGREGATION_TYPE") String aggregationType,
			String aggregationField, String aggregationRelatedField, Map<String, String> inheritanceMapping,
			Date dateCreated, Date dateUpdated, String createdBy, String lastUpdatedBy, String workflow, String prefix,
			String suffix, String numericFormat, Map<String, String> fieldsMapping, String toCurrency,
			String fromCurrency, String dateIncurred, List<Condition> conditions) {
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
		this.dataType = dataType;
		this.visibility = visibility;
		this.required = required;
		this.internal = internal;
		this.notEditable = notEditable;
		this.maxSize = maxSize;
		this.autonumberGeneration = autonumberGeneration;
		this.autonumberStartingNumber = autonumberStartingNumber;
		this.picklistDisplay = picklistDisplay;
		this.picklistFirstValue = picklistFirstValue;
		this.picklistValues = picklistValues;
		this.formula = formula;
		this.messages = messages;
		this.groupId = groupId;
		this.dataFilter = dataFilter;
		this.aggregationType = aggregationType;
		this.aggregationField = aggregationField;
		this.aggregationRelatedField = aggregationRelatedField;
		this.inheritanceMapping = inheritanceMapping;
		this.dateCreated = dateCreated;
		this.dateUpdated = dateUpdated;
		this.createdBy = createdBy;
		this.lastUpdatedBy = lastUpdatedBy;
		this.workflow = workflow;
		this.prefix = prefix;
		this.suffix = suffix;
		this.numericFormat = numericFormat;
		this.fieldsMapping = fieldsMapping;
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

	public int getMaxSize() {
		return maxSize;
	}

	public void setMaxSize(int maxSize) {
		this.maxSize = maxSize;
	}

	public Boolean getAutonumberGeneration() {
		return autonumberGeneration;
	}

	public void setAutonumberGeneration(Boolean autonumberGeneration) {
		this.autonumberGeneration = autonumberGeneration;
	}

	public int getAutonumberStartingNumber() {
		return autonumberStartingNumber;
	}

	public void setAutonumberStartingNumber(int autonumberStartingNumber) {
		this.autonumberStartingNumber = autonumberStartingNumber;
	}

	public Boolean getPicklistDisplay() {
		return picklistDisplay;
	}

	public void setPicklistDisplay(Boolean picklistDisplay) {
		this.picklistDisplay = picklistDisplay;
	}

	public Boolean getPicklistFirstValue() {
		return picklistFirstValue;
	}

	public void setPicklistFirstValue(Boolean picklistFirstValue) {
		this.picklistFirstValue = picklistFirstValue;
	}

	public List<String> getPicklistValues() {
		return picklistValues;
	}

	public void setPicklistValues(List<String> picklistValues) {
		this.picklistValues = picklistValues;
	}

	public String getFormula() {
		return formula;
	}

	public void setFormula(String formula) {
		this.formula = formula;
	}

	public List<DiscussionMessage> getMessages() {
		return messages;
	}

	public void setMessages(List<DiscussionMessage> messages) {
		this.messages = messages;
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

	public Date getDateCreated() {
		return dateCreated;
	}

	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}

	public Date getDateUpdated() {
		return dateUpdated;
	}

	public void setDateUpdated(Date dateUpdated) {
		this.dateUpdated = dateUpdated;
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

	public String getWorkflow() {
		return workflow;
	}

	public void setWorkflow(String workflow) {
		this.workflow = workflow;
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

	public Map<String, String> getFieldsMapping() {
		return fieldsMapping;
	}

	public void setFieldsMapping(Map<String, String> fieldsMapping) {
		this.fieldsMapping = fieldsMapping;
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
