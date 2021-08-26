package com.ngdesk.data.dao;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Relationship {

	@JsonProperty("DATA_ID")
	private String dataId;

	@JsonProperty("PRIMARY_DISPLAY_FIELD")
	private String primaryDisplayField;

	public Relationship() {
	}

	public Relationship(String dataId, String primaryDisplayField) {
		this.dataId = dataId;
		this.primaryDisplayField = primaryDisplayField;
	}

	public String getDataId() {
		return dataId;
	}

	public void setDataId(String dataId) {
		this.dataId = dataId;
	}

	public String getPrimaryDisplayField() {
		return primaryDisplayField;
	}

	public void setPrimaryDisplayField(String primaryDisplayField) {
		this.primaryDisplayField = primaryDisplayField;
	}

}
