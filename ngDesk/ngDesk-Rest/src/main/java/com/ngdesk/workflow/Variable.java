package com.ngdesk.workflow;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Variable {

	@JsonProperty("FIELD")
	@NotNull(message = "FIELD_ID_NULL")
	@Size(min = 1, message = "FIELD_ID_BLANK")
	private String fieldId;

	@JsonProperty("NAME")
	@NotNull(message = "NAME_NOT_NULL")
	@Size(min = 1, message = "NAME_BLANK")
	private String name;

	@JsonProperty("DISPLAY_LABEL")
	@NotNull(message = "DISPLAY_LABEL_NOT_NULL")
	@Size(min = 1, message = "DISPLAY_LABEL_EMPTY")
	private String displayLabel;

	@JsonProperty("DATA_TYPE")
	@NotNull(message = "DATA_TYPE_NOT_NULL")
	private DataType dataType;

	public Variable() {

	}

	public Variable(@NotNull(message = "FIELD_ID_NULL") @Size(min = 1, message = "FIELD_ID_BLANK") String fieldId,
			@NotNull(message = "NAME_NOT_NULL") @Size(min = 1, message = "NAME_BLANK") String name,
			@NotNull(message = "DISPLAY_LABEL_NOT_NULL") @Size(min = 1, message = "DISPLAY_LABEL_EMPTY") String displayLabel,
			@NotNull(message = "DATA_TYPE_NOT_NULL") DataType dataType) {
		super();
		this.fieldId = fieldId;
		this.name = name;
		this.displayLabel = displayLabel;
		this.dataType = dataType;
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

}
