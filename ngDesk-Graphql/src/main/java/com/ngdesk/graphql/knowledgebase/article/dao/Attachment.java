package com.ngdesk.graphql.knowledgebase.article.dao;

public class Attachment {

	private String attachmentUuid;
	private String file;
	private String hash;
	private String fileName;

	public Attachment() {
	}

	public Attachment(String file, String hash, String attachmentUuid, String fileName) {
		super();
		this.file = file;
		this.hash = hash;
		this.attachmentUuid = attachmentUuid;
		this.fileName = fileName;
	}

	public String getFile() {
		return file;
	}

	public void setFile(String file) {
		this.file = file;
	}

	public String getHash() {
		return hash;
	}

	public void setHash(String hash) {
		this.hash = hash;
	}

	public String getAttachmentUuid() {
		return attachmentUuid;
	}

	public void setAttachmentUuid(String attachmentUuid) {
		this.attachmentUuid = attachmentUuid;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
}
