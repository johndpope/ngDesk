package com.ngdesk.sam.software;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SoftwareInstallations {

	@JsonProperty("ENTRIES")
	List<SoftwareInstallation> softwareInstallations;

	public SoftwareInstallations() {

	}

	public SoftwareInstallations(List<SoftwareInstallation> softwareInstallations) {
		super();
		this.softwareInstallations = softwareInstallations;
	}

	public List<SoftwareInstallation> getSoftwareInstallations() {
		return softwareInstallations;
	}

	public void setSoftwareInstallations(List<SoftwareInstallation> softwareInstallations) {
		this.softwareInstallations = softwareInstallations;
	}

}
