package com.ngdesk.graphql.campaigns.dao;

import javax.validation.constraints.Pattern;

import org.springframework.data.mongodb.core.mapping.Field;

public class RowSettings {
	@Field("COLUMN_LAYOUT")
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
