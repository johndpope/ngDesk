package com.ngdesk.data.dao;

public class Swidtag {

	private String filename;

	private String fileContent;

	private String companyId;

	private String assetId;

	public Swidtag() {

	}

	public Swidtag(String filename, String fileContent) {
		super();
		this.filename = filename;
		this.fileContent = fileContent;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public String getFileContent() {
		return fileContent;
	}

	public void setFileContent(String fileContent) {
		this.fileContent = fileContent;
	}

	public String getCompanyId() {
		return companyId;
	}

	public void setCompanyId(String companyId) {
		this.companyId = companyId;
	}

	public String getAssetId() {
		return assetId;
	}

	public void setAssetId(String assetId) {
		this.assetId = assetId;
	}

}
