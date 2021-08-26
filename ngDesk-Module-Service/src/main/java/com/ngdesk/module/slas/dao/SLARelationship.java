package com.ngdesk.module.slas.dao;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SLARelationship {
	@JsonProperty("DATA_ID")
	private String dataId;

	@JsonProperty("NAME")
	private String name;

	public SLARelationship() {

	}

	public SLARelationship(String dataId, String name) {
		super();
		this.dataId = dataId;
		this.name = name;
	}

	public String getDataId() {
		return dataId;
	}

	public void setDataId(String dataId) {
		this.dataId = dataId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
