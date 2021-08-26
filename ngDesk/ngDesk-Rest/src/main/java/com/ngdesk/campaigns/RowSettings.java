package com.ngdesk.campaigns;

import java.util.List;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RowSettings {
	@JsonProperty("COLUMN_LAYOUT")
	@Pattern(regexp = "^(1|2|3|1/3 : 2/3|2/3 : 1/3)$", message = "INVALID_CAMPAIGN_COLUMN_LAYOUT_TYPE")
	private String columnLayout;

	public RowSettings() {
	}

	public RowSettings(
			@Pattern(regexp = "^(1|2|3|1/3 : 2/3|2/3 : 1/3)$", message = "INVALID_CAMPAIGN_COLUMN_LAYOUT_TYPE") String columnLayout) {
		super();
		this.columnLayout = columnLayout;
	}

	public String getColumnLayout() {
		return columnLayout;
	}

	public void setColumnLayout(String columnLayout) {
		this.columnLayout = columnLayout;
	}
}
