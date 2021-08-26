package com.ngdesk.sam.controllers.dao;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Version {

	@JsonProperty("CONTROLLER_VERSION")
	private Integer version;

	@JsonProperty("SUB_APPS")
	private List<SubAppVersion> subAppVersions;

	public Version() {

	}

	public Version(Integer version, List<SubAppVersion> subAppVersions) {
		this.version = version;
		this.subAppVersions = subAppVersions;
	}

	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}

	public List<SubAppVersion> getSubAppVersions() {
		return subAppVersions;
	}

	public void setSubAppVersions(List<SubAppVersion> subAppVersions) {
		this.subAppVersions = subAppVersions;
	}

}
