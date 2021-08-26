export interface DATATYPE {
	DISPLAY: string;
	BACKEND: string;
}

export interface Field {
	FIELD_ID: string;
	NAME: string;
	DISPLAY_LABEL: string;
	HELP_TEXT?: string;
	DEFAULT_VALUE?: string;
	LENGTH: number;
	DECIMAL_PLACES: number;
	MAX_SIZE: number;
	MASK_TYPE?: any;
	MASK_CHARACTER?: any;
	EXAMPLE?: string;
	RELATIONSHIP_TYPE?: any;
	AUTO_NUMBER_GENERATION: boolean;
	AUTO_NUMBER_STARTING_NUMBER: number;
	GEOLOCATION_LATITUDE_LONGTITUDE: string;
	PICKLIST: string;
	PICKLIST_GLOBAL_LIST?: string;
	PICKLIST_VALUES?: string[];
	PICKLIST_USE_FIRST_VALUE: boolean;
	PICKLIST_DISPLAY_ALPHABETICALLY: boolean;
	PICKLIST_RESTRICT: boolean;
	PRIMARY_DISPLAY_FIELD?: string;
	RELATIONSHIP_FIELD?: string;
	FORMULA: string;
	MODULE?: string;
	VISIBILITY: boolean;
	IS_LIST_TEXT_UNIQUE: boolean;
	REQUIRED: boolean;
	DATA_TYPE: DATATYPE;
	ORDER: number;
	TYPE: number;
	DATE_CREATED: Date;
	LAST_UPDATED: Date;
	LAST_UPDATED_BY?: any;
	CREATED_BY?: string;
	MESSAGES?: string;
	INTERNAL: boolean;
	NOT_EDITABLE: boolean;
	GROUP_ID?: string;
	PRIMARY_DISPLAY_FIELD_NAME?: string;
	TO_CURRENCY?: string;
	FROM_CURRENCY?: string;
	DATE_INCURRED?: string;
}
