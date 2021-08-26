package com.ngdesk.workflow;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DataType {
	@JsonProperty("DISPLAY")
	@NotNull(message = "DISPLAY_NULL")
	@Size(min = 1, message = "DISPLAY_BLANK")
	private String displayLabel;

	@JsonProperty("BACKEND")
	@NotNull(message = "BACKEND_NULL")
	@Size(min = 1, message = "BACKEND_EMPTY")
	private String bakend;

	@JsonProperty("DESCRIPTION")
	private String description;

	public DataType() {

	}

	public DataType(@NotNull(message = "DISPLAY_NULL") @Size(min = 1, message = "DISPLAY_BLANK") String displayLabel,
			@NotNull(message = "BACKEND_NULL") @Size(min = 1, message = "BACKEND_EMPTY") String bakend,
			String description) {
		super();
		this.displayLabel = displayLabel;
		this.bakend = bakend;
		this.description = description;
	}

	public String getDisplayLabel() {
		return displayLabel;
	}

	public void setDisplayLabel(String displayLabel) {
		this.displayLabel = displayLabel;
	}

	public String getBakend() {
		return bakend;
	}

	public void setBakend(String bakend) {
		this.bakend = bakend;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

}
