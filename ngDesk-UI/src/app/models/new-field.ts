export class NewField {
	constructor(
		private AUTO_NUMBER_DISPLAY_FORMAT: string,
		private AUTO_NUMBER_GENERATION: boolean,
		private AUTO_NUMBER_STARTING_NUMBER: number,
		private CREATED_BY: string,
		private DECIMAL_PLACES: number,
		private MAX_SIZE: number,
		private DATA_TYPE: {},
		private DATE_CREATED: Date,
		private DISPLAY_LABEL: string,
		private DEFAULT_VALUE: any,
		private FORMULA: any,
		private GEOLOCATION_LATITUDE_LONGTITUDE: string,
		private GROUP_ID: string,
		private HELP_TEXT: string,
		private INTERNAL: boolean,
		private LAST_UPDATED: Date,
		private LAST_UPDATED_BY: string,
		private MODULE: string,
		private NAME: string,
		private MESSAGES: string,
		private PICKLIST: string,
		private PICKLIST_VALUES: string[],
		private PICKLIST_GLOBAL_LIST: string,
		private PRIMARY_DISPLAY_FIELD: string,
		private PICKLIST_USE_FIRST_VALUE: boolean,
		private PICKLIST_DISPLAY_ALPHABETICALLY: boolean,
		private PICKLIST_RESTRICT: boolean,
		private ORDER: number,
		private RELATIONSHIP_FIELD: string,
		private RELATIONSHIP_TYPE: string,
		private NOT_EDITABLE: boolean,
		private REQUIRED: boolean,
		private MASK_TYPE: any,
		private MASK_CHARACTER: string,
		private EXAMPLE: string,
		private LENGTH: number,
		private TYPE: number,
		private VISIBILITY: boolean,
		private IS_LIST_TEXT_UNIQUE: boolean,
		private WORKFLOW: string,
		private SHOW_PREVIEW_OF_FILES: boolean,
		private FIELD_ID?: string,
		private DATA_FILTER?: any,
		private AGGREGATION_FIELD?: string,
		private AGGREGATION_RELATED_FIELD?: string,
		private AGGREGATION_TYPE?: string,
		private INHERITANCE_MAPPING?: any,
		private NUMERIC_FORMAT?: string,
		private PREFIX?: string,
		private SUFFIX?: string,
		private CONDITIONS?: [],
		private LIST_FORMULA?: []
	) {}

	get listFormula() {
		return this.LIST_FORMULA;
	}

	set listFormula(value) {
		this.LIST_FORMULA = value;
	}

	get aggregationField() {
		return this.AGGREGATION_FIELD;
	}

	set aggregationField(value) {
		this.AGGREGATION_FIELD = value;
	}

	get inheritanceMapping() {
		return this.INHERITANCE_MAPPING;
	}

	set inheritanceMapping(value) {
		this.INHERITANCE_MAPPING = value;
	}

	get aggregationType() {
		return this.AGGREGATION_TYPE;
	}

	set aggregationType(value) {
		this.AGGREGATION_TYPE = value;
	}

	get aggregationRelatedField() {
		return this.AGGREGATION_RELATED_FIELD;
	}

	set aggregationRelatedField(value) {
		this.AGGREGATION_RELATED_FIELD = value;
	}
	get dataFilter() {
		return this.DATA_FILTER;
	}

	set dataFilter(value) {
		this.DATA_FILTER = value;
	}

	get workflow() {
		return this.WORKFLOW;
	}

	set workflow(value) {
		this.WORKFLOW = value;
	}

	get isListTextUnique() {
		return this.IS_LIST_TEXT_UNIQUE;
	}

	set isListTextUnique(value) {
		this.IS_LIST_TEXT_UNIQUE = value;
	}

	get autoNumberDisplayFormat() {
		return this.AUTO_NUMBER_DISPLAY_FORMAT;
	}

	set autoNumberDisplayFormat(value) {
		this.AUTO_NUMBER_DISPLAY_FORMAT = value;
	}

	get autoNumberGeneration() {
		return this.AUTO_NUMBER_GENERATION;
	}

	set autoNumberGeneration(value) {
		this.AUTO_NUMBER_GENERATION = value;
	}

	get autoNumberStartingNumber() {
		return this.AUTO_NUMBER_STARTING_NUMBER;
	}

	set autoNumberStartingNumber(value) {
		this.AUTO_NUMBER_STARTING_NUMBER = value;
	}

	get maxSize() {
		return this.MAX_SIZE;
	}

	set maxSize(value) {
		this.MAX_SIZE = value;
	}

	get createdBy() {
		return this.CREATED_BY;
	}

	set createdBy(value) {
		this.CREATED_BY = value;
	}

	get dataType() {
		return this.DATA_TYPE;
	}

	set dataType(value) {
		this.DATA_TYPE = value;
	}

	get dateCreated() {
		return this.DATE_CREATED;
	}

	set dateCreated(value) {
		this.DATE_CREATED = value;
	}

	get decimalPlaces() {
		return this.DECIMAL_PLACES;
	}

	set decimalPlaces(value) {
		this.DECIMAL_PLACES = value;
	}

	get dispayLabel() {
		return this.DISPLAY_LABEL;
	}

	set dispayLabel(value) {
		this.DISPLAY_LABEL = value;
	}

	get defaultValue() {
		return this.DEFAULT_VALUE;
	}

	set defaultValue(value) {
		this.DEFAULT_VALUE = value;
	}

	get example() {
		return this.EXAMPLE;
	}

	set example(value) {
		this.EXAMPLE = value;
	}

	get fieldId() {
		return this.FIELD_ID;
	}

	set fieldId(value) {
		this.FIELD_ID = value;
	}

	get formula() {
		return this.FORMULA;
	}

	set formula(value) {
		this.FORMULA = value;
	}

	get geolocationLatitudeLongitude() {
		return this.GEOLOCATION_LATITUDE_LONGTITUDE;
	}

	set geolocationLatitudeLongitude(value) {
		this.GEOLOCATION_LATITUDE_LONGTITUDE = value;
	}

	get groupId() {
		return this.GROUP_ID;
	}

	set groupId(value) {
		this.GROUP_ID = value;
	}

	get helpText() {
		return this.HELP_TEXT;
	}

	set helpText(value) {
		this.HELP_TEXT = value;
	}

	get internal() {
		return this.INTERNAL;
	}

	set internal(value) {
		this.INTERNAL = value;
	}

	get lastUpdated() {
		return this.LAST_UPDATED;
	}

	set lastUpdated(value) {
		this.LAST_UPDATED = value;
	}

	get lastUpdatedBy() {
		return this.LAST_UPDATED_BY;
	}

	set lastUpdatedBy(value) {
		this.LAST_UPDATED_BY = value;
	}

	get length() {
		return this.LENGTH;
	}

	set length(value) {
		this.LENGTH = value;
	}

	get maskType() {
		return this.MASK_TYPE;
	}

	set maskType(value) {
		this.MASK_TYPE = value;
	}

	get maskCharacter() {
		return this.MASK_CHARACTER;
	}

	set maskCharacter(value) {
		this.MASK_CHARACTER = value;
	}

	get message() {
		return this.MESSAGES;
	}

	set message(value) {
		this.MESSAGES = value;
	}

	get module() {
		return this.MODULE;
	}

	set module(value) {
		this.MODULE = value;
	}

	get name() {
		return this.NAME;
	}

	set name(value) {
		this.NAME = value;
	}

	get notEditable() {
		return this.NOT_EDITABLE;
	}

	set notEditable(value) {
		this.NOT_EDITABLE = value;
	}

	get order() {
		return this.ORDER;
	}

	set order(value) {
		this.ORDER = value;
	}

	get picklist() {
		return this.PICKLIST;
	}

	set picklist(value) {
		this.PICKLIST = value;
	}

	get picklistValues() {
		return this.PICKLIST_VALUES;
	}

	set picklistValues(value) {
		this.PICKLIST_VALUES = value;
	}

	get picklistGlobalList() {
		return this.PICKLIST_GLOBAL_LIST;
	}

	set picklistGlobalList(value) {
		this.PICKLIST_GLOBAL_LIST = value;
	}

	get picklistUseFirstValue() {
		return this.PICKLIST_USE_FIRST_VALUE;
	}

	set picklistUseFirstValue(value) {
		this.PICKLIST_USE_FIRST_VALUE = value;
	}

	get picklistDisplayAlphabetically() {
		return this.PICKLIST_DISPLAY_ALPHABETICALLY;
	}

	set picklistDisplayAlphabetically(value) {
		this.PICKLIST_DISPLAY_ALPHABETICALLY = value;
	}

	get picklistRestrict() {
		return this.PICKLIST_RESTRICT;
	}

	set picklistRestrict(value) {
		this.PICKLIST_RESTRICT = value;
	}

	get primaryDisplayField() {
		return this.PRIMARY_DISPLAY_FIELD;
	}

	set primaryDisplayField(value) {
		this.PRIMARY_DISPLAY_FIELD = value;
	}

	get relationshipType() {
		return this.RELATIONSHIP_TYPE;
	}

	set relationshipType(value) {
		this.RELATIONSHIP_TYPE = value;
	}

	get relationshipField() {
		return this.RELATIONSHIP_FIELD;
	}

	set relationshipField(value) {
		this.RELATIONSHIP_FIELD = value;
	}

	get required() {
		return this.REQUIRED;
	}

	set required(value) {
		this.REQUIRED = value;
	}

	get type() {
		return this.TYPE;
	}

	set type(value) {
		this.TYPE = value;
	}

	get visibility() {
		return this.VISIBILITY;
	}

	set visibility(value) {
		this.VISIBILITY = value;
	}

	get showPreviewOfFiles() {
		return this.SHOW_PREVIEW_OF_FILES;
	}

	set showPreviewOfFiles(value) {
		this.SHOW_PREVIEW_OF_FILES = value;
	}

	get prefix() {
		return this.PREFIX;
	}

	set prefix(value) {
		this.PREFIX = value;
	}

	get suffix() {
		return this.SUFFIX;
	}

	set suffix(value) {
		this.SUFFIX = value;
	}

	get numericFormat() {
		return this.NUMERIC_FORMAT;
	}

	set numericFormat(value) {
		this.NUMERIC_FORMAT = value;
	}

	get conditions() {
		return this.CONDITIONS;
	}

	set conditions(value) {
		this.CONDITIONS = value;
	}
}
