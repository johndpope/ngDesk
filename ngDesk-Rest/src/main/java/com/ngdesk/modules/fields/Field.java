package com.ngdesk.modules.fields;

import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.ngdesk.modules.rules.Condition;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Field {

	@JsonProperty("FIELD_ID")
	private String fieldId;

	@JsonProperty("NAME")
	@NotNull(message = "FIELD_NAME_NOT_NULL")
	@Size(min = 1, message = "FIELD_NAME_NOT_EMPTY")
	private String name;

	@JsonProperty("DISPLAY_LABEL")
	@NotNull(message = "DISPLAY_LABEL_NOT_NULL")
	@Size(min = 1, message = "DISPLAY_LABEL_EMPTY")
	private String displayLabel;

	@JsonProperty("HELP_TEXT")
	private String helpText;

	@JsonProperty("DEFAULT_VALUE")
	private String defaultValue;

	@JsonProperty("LENGTH")
	private int length;

	@JsonProperty("DECIMAL_PLACES")
	private int decimalPlaces;

	@JsonProperty("MAX_SIZE")
	private int maxSize;

	@JsonProperty("MASK_TYPE")
	private String maskType;

	@JsonProperty("MASK_CHARACTER")
	private String maskCharacter;

	@JsonProperty("EXAMPLE")
	private String example;

	@JsonProperty("RELATIONSHIP_TYPE")
	@Pattern(regexp = "One to One|One to Many|Many to Many|Many to One", message = "NOT_VALID_RELATIONSHIP_TYPE")
	private String relationshipType;

	@JsonProperty("AUTO_NUMBER_GENERATION")
	private boolean autonumberGeneration;

	@JsonProperty("AUTO_NUMBER_STARTING_NUMBER")
	private long autonumberStartingNumber;

	@JsonProperty("GEOLOCATION_LATITUDE_LONGTITUDE")
	private String coordinates;

	@JsonProperty("PICKLIST")
	private String picklist;

	@JsonProperty("PICKLIST_GLOBAL_LIST")
	private String picklistGloballist;

	@JsonProperty("PICKLIST_VALUES")
	private List<String> picklistValues;

	@JsonProperty("PICKLIST_USE_FIRST_VALUE")
	private boolean picklistFirstValue;

	@JsonProperty("PICKLIST_DISPLAY_ALPHABETICALLY")
	private boolean picklistDisplay;

	@JsonProperty("PICKLIST_RESTRICT")
	private boolean picklistRestrict;

	@JsonProperty("PRIMARY_DISPLAY_FIELD")
	private String lookupRelationshipField;

	@JsonProperty("RELATIONSHIP_FIELD")
	private String relationshipField;

	@JsonProperty("FORMULA")
	private String formula;

	@JsonProperty("MODULE")
	private String module;

	@JsonProperty("VISIBILITY")
	private boolean visibility;

	@JsonProperty("REQUIRED")
	private boolean required;

	@JsonProperty("DATA_TYPE")
	@NotNull(message = "DATATYPE_NOT_NULL")
	@Valid
	private DataType datatypes;

	@JsonProperty("ORDER")
	private int order;

	@JsonProperty("TYPE")
	private int type;

	@JsonProperty("DATE_CREATED")
	private Date dateCreated;

	@JsonProperty("LAST_UPDATED")
	private Date dateUpdated;

	@JsonProperty("LAST_UPDATED_BY")
	private String lastUpdatedBy;

	@JsonProperty("CREATED_BY")
	private String createdBy;

	@JsonProperty("MESSAGES")
	private List<DiscussionMessage> messages;

	@JsonProperty("INTERNAL")
	private boolean internal;

	@JsonProperty("NOT_EDITABLE")
	private boolean notEditable;

	@JsonProperty("GROUP_ID")
	private String groupId;

	@JsonProperty("IS_LIST_TEXT_UNIQUE")
	private boolean listTextUnique;

	@JsonProperty("WORKFLOW")
	private String workflow;

	@JsonProperty("DATA_FILTER")
	private DataFilter dataFilter;

	@JsonProperty("AGGREGATION_TYPE")
	@Pattern(regexp = "sum", message = "NOT_VALID_AGGREGATION_TYPE")
	private String aggregationType;

	@JsonProperty("AGGREGATION_FIELD")
	private String aggregationField;

	@JsonProperty("AGGREGATION_RELATED_FIELD")
	private String aggregationRelatedField;

	@JsonProperty("INHERITANCE_MAPPING")
	private Map<String, String> inheritanceMapping;

	@JsonProperty("FIELDS_MAPPING")
	private Map<String, String> fieldsMapping;

	@JsonProperty("UNIQUE")
	private boolean unique;

	@JsonProperty("PREFIX")
	private String prefix;

	@JsonProperty("SUFFIX")
	private String suffix;

	@JsonProperty("NUMERIC_FORMAT")
	private String numericFormat;

	@JsonProperty("TO_CURRENCY")
	private String toCurrency;

	@JsonProperty("FROM_CURRENCY")
	private String fromCurrency;

	@JsonProperty("DATE_INCURRED")
	private String dateIncurred;

	@JsonProperty("CONDITIONS")
	List<Condition> conditions;
	
	@JsonProperty("RESTRICT_PAST_DATE")
	private boolean restrictPastDate;

	public Field() {
	}

	public Field(String fieldId,
			@NotNull(message = "FIELD_NAME_NOT_NULL") @Size(min = 1, message = "FIELD_NAME_NOT_EMPTY") String name,
			@NotNull(message = "DISPLAY_LABEL_NOT_NULL") @Size(min = 1, message = "DISPLAY_LABEL_EMPTY") String displayLabel,
			String helpText, String defaultValue, int length, int decimalPlaces, int maxSize, String maskType,
			String maskCharacter, String example,
			@Pattern(regexp = "One to One|One to Many|Many to Many|Many to One", message = "NOT_VALID_RELATIONSHIP_TYPE") String relationshipType,
			boolean autonumberGeneration, long autonumberStartingNumber, String coordinates, String picklist,
			String picklistGloballist, List<String> picklistValues, boolean picklistFirstValue, boolean picklistDisplay,
			boolean picklistRestrict, String lookupRelationshipField, String relationshipField, String formula,
			String module, boolean visibility, boolean required,
			@NotNull(message = "DATATYPE_NOT_NULL") @Valid DataType datatypes, int order, int type, Date dateCreated,
			Date dateUpdated, String lastUpdatedBy, String createdBy, List<DiscussionMessage> messages,
			boolean internal, boolean notEditable, String groupId, boolean listTextUnique, String workflow,
			DataFilter dataFilter,
			@Pattern(regexp = "sum", message = "NOT_VALID_AGGREGATION_TYPE") String aggregationType,
			String aggregationField, String aggregationRelatedField, Map<String, String> inheritanceMapping,
			Map<String, String> fieldsMapping, boolean unique, String prefix, String suffix, String numericFormat,
			String toCurrency, String fromCurrency, String dateIncurred, List<Condition> conditions,boolean restrictPastDate) {
		super();
		this.fieldId = fieldId;
		this.name = name;
		this.displayLabel = displayLabel;
		this.helpText = helpText;
		this.defaultValue = defaultValue;
		this.length = length;
		this.decimalPlaces = decimalPlaces;
		this.maxSize = maxSize;
		this.maskType = maskType;
		this.maskCharacter = maskCharacter;
		this.example = example;
		this.relationshipType = relationshipType;
		this.autonumberGeneration = autonumberGeneration;
		this.autonumberStartingNumber = autonumberStartingNumber;
		this.coordinates = coordinates;
		this.picklist = picklist;
		this.picklistGloballist = picklistGloballist;
		this.picklistValues = picklistValues;
		this.picklistFirstValue = picklistFirstValue;
		this.picklistDisplay = picklistDisplay;
		this.picklistRestrict = picklistRestrict;
		this.lookupRelationshipField = lookupRelationshipField;
		this.relationshipField = relationshipField;
		this.formula = formula;
		this.module = module;
		this.visibility = visibility;
		this.required = required;
		this.datatypes = datatypes;
		this.order = order;
		this.type = type;
		this.dateCreated = dateCreated;
		this.dateUpdated = dateUpdated;
		this.lastUpdatedBy = lastUpdatedBy;
		this.createdBy = createdBy;
		this.messages = messages;
		this.internal = internal;
		this.notEditable = notEditable;
		this.groupId = groupId;
		this.listTextUnique = listTextUnique;
		this.workflow = workflow;
		this.dataFilter = dataFilter;
		this.aggregationType = aggregationType;
		this.aggregationField = aggregationField;
		this.aggregationRelatedField = aggregationRelatedField;
		this.inheritanceMapping = inheritanceMapping;
		this.fieldsMapping = fieldsMapping;
		this.unique = unique;
		this.prefix = prefix;
		this.suffix = suffix;
		this.numericFormat = numericFormat;
		this.toCurrency = toCurrency;
		this.fromCurrency = fromCurrency;
		this.dateIncurred = dateIncurred;
		this.conditions = conditions;
		this.restrictPastDate = restrictPastDate;
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

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}

	public int getDecimalPlaces() {
		return decimalPlaces;
	}

	public void setDecimalPlaces(int decimalPlaces) {
		this.decimalPlaces = decimalPlaces;
	}

	public int getMaxSize() {
		return maxSize;
	}

	public void setMaxSize(int maxSize) {
		this.maxSize = maxSize;
	}

	public String getMaskType() {
		return maskType;
	}

	public void setMaskType(String maskType) {
		this.maskType = maskType;
	}

	public String getMaskCharacter() {
		return maskCharacter;
	}

	public void setMaskCharacter(String maskCharacter) {
		this.maskCharacter = maskCharacter;
	}

	public String getExample() {
		return example;
	}

	public void setExample(String example) {
		this.example = example;
	}

	public String getRelationshipType() {
		return relationshipType;
	}

	public void setRelationshipType(String relationshipType) {
		this.relationshipType = relationshipType;
	}

	public boolean isAutonumberGeneration() {
		return autonumberGeneration;
	}

	public void setAutonumberGeneration(boolean autonumberGeneration) {
		this.autonumberGeneration = autonumberGeneration;
	}

	public long getAutonumberStartingNumber() {
		return autonumberStartingNumber;
	}

	public void setAutonumberStartingNumber(long autonumberStartingNumber) {
		this.autonumberStartingNumber = autonumberStartingNumber;
	}

	public String getCoordinates() {
		return coordinates;
	}

	public void setCoordinates(String coordinates) {
		this.coordinates = coordinates;
	}

	public String getPicklist() {
		return picklist;
	}

	public void setPicklist(String picklist) {
		this.picklist = picklist;
	}

	public String getPicklistGloballist() {
		return picklistGloballist;
	}

	public void setPicklistGloballist(String picklistGloballist) {
		this.picklistGloballist = picklistGloballist;
	}

	public List<String> getPicklistValues() {
		return picklistValues;
	}

	public void setPicklistValues(List<String> picklistValues) {
		this.picklistValues = picklistValues;
	}

	public boolean isPicklistFirstValue() {
		return picklistFirstValue;
	}

	public void setPicklistFirstValue(boolean picklistFirstValue) {
		this.picklistFirstValue = picklistFirstValue;
	}

	public boolean isPicklistDisplay() {
		return picklistDisplay;
	}

	public void setPicklistDisplay(boolean picklistDisplay) {
		this.picklistDisplay = picklistDisplay;
	}

	public boolean isPicklistRestrict() {
		return picklistRestrict;
	}

	public void setPicklistRestrict(boolean picklistRestrict) {
		this.picklistRestrict = picklistRestrict;
	}

	public String getLookupRelationshipField() {
		return lookupRelationshipField;
	}

	public void setLookupRelationshipField(String lookupRelationshipField) {
		this.lookupRelationshipField = lookupRelationshipField;
	}

	public String getRelationshipField() {
		return relationshipField;
	}

	public void setRelationshipField(String relationshipField) {
		this.relationshipField = relationshipField;
	}

	public String getFormula() {
		return formula;
	}

	public void setFormula(String formula) {
		this.formula = formula;
	}

	public String getModule() {
		return module;
	}

	public void setModule(String module) {
		this.module = module;
	}

	public boolean isVisibility() {
		return visibility;
	}

	public void setVisibility(boolean visibility) {
		this.visibility = visibility;
	}

	public boolean isRequired() {
		return required;
	}

	public void setRequired(boolean required) {
		this.required = required;
	}

	public DataType getDatatypes() {
		return datatypes;
	}

	public void setDatatypes(DataType datatypes) {
		this.datatypes = datatypes;
	}

	public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		this.order = order;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
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

	public String getLastUpdatedBy() {
		return lastUpdatedBy;
	}

	public void setLastUpdatedBy(String lastUpdatedBy) {
		this.lastUpdatedBy = lastUpdatedBy;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public List<DiscussionMessage> getMessages() {
		return messages;
	}

	public void setMessages(List<DiscussionMessage> messages) {
		this.messages = messages;
	}

	public boolean isInternal() {
		return internal;
	}

	public void setInternal(boolean internal) {
		this.internal = internal;
	}

	public boolean isNotEditable() {
		return notEditable;
	}

	public void setNotEditable(boolean notEditable) {
		this.notEditable = notEditable;
	}

	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	public boolean isListTextUnique() {
		return listTextUnique;
	}

	public void setListTextUnique(boolean listTextUnique) {
		this.listTextUnique = listTextUnique;
	}

	public String getWorkflow() {
		return workflow;
	}

	public void setWorkflow(String workflow) {
		this.workflow = workflow;
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

	public Map<String, String> getFieldsMapping() {
		return fieldsMapping;
	}

	public void setFieldsMapping(Map<String, String> fieldsMapping) {
		this.fieldsMapping = fieldsMapping;
	}

	public boolean isUnique() {
		return unique;
	}

	public void setUnique(boolean unique) {
		this.unique = unique;
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

	public boolean isRestrictPastDate() {
		return restrictPastDate;
	}

	public void setRestrictPastDate(boolean restrictPastDate) {
		this.restrictPastDate = restrictPastDate;
	}
	

}
