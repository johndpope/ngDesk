package com.ngdesk.sam.controllers.dao;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SubAppVersion {

	@JsonProperty("NAME")
	private String name;

	@JsonProperty("VERSION")
	private Integer version;

	public SubAppVersion() {

	}

	public SubAppVersion(String name, Integer version) {
		this.name = name;
		this.version = version;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}

}
