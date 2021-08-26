package com.ngdesk.sam.software;

import java.util.List;

import org.springframework.data.elasticsearch.annotations.Field;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SoftwareInstallation {

	@JsonProperty("DATA_ID")
	@Field("DATA_ID")
	private String dataId;

	@JsonProperty("DISPLAY_NAME")
	@Field("DISPLAY_NAME")
	private String softwareName;

	@JsonProperty("INSTALL_LOCATION")
	@Field("INSTALL_LOCATION")
	private String installPath;

	@JsonProperty("FOUND_BY")
	@Field("FOUND_BY")
	private List<String> foundBy;

	@JsonProperty("EDITION")
	@Field("EDITION")
	private String edition;

	@JsonProperty("PUBLISHER")
	@Field("PUBLISHER")
	private String publisher;

	@JsonProperty("VERSION")
	@Field("VERSION")
	private String version;

	@JsonProperty("ASSET")
	@Field("ASSET")
	private String asset;

	public SoftwareInstallation() {

	}

	public SoftwareInstallation(String dataId, String softwareName, String installPath, List<String> foundBy,
			String edition, String publisher, String version, String asset) {
		super();
		this.dataId = dataId;
		this.softwareName = softwareName;
		this.installPath = installPath;
		this.foundBy = foundBy;
		this.edition = edition;
		this.publisher = publisher;
		this.version = version;
		this.asset = asset;
	}

	public String getDataId() {
		return dataId;
	}

	public void setDataId(String dataId) {
		this.dataId = dataId;
	}

	public String getSoftwareName() {
		return softwareName;
	}

	public void setSoftwareName(String softwareName) {
		this.softwareName = softwareName;
	}

	public String getInstallPath() {
		return installPath;
	}

	public void setInstallPath(String installPath) {
		this.installPath = installPath;
	}

	public List<String> getFoundBy() {
		return foundBy;
	}

	public void setFoundBy(List<String> foundBy) {
		this.foundBy = foundBy;
	}

	public String getEdition() {
		return edition;
	}

	public void setEdition(String edition) {
		this.edition = edition;
	}

	public String getPublisher() {
		return publisher;
	}

	public void setPublisher(String publisher) {
		this.publisher = publisher;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getAsset() {
		return asset;
	}

	public void setAsset(String asset) {
		this.asset = asset;
	}

}
