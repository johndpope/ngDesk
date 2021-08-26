package com.ngdesk.data.dao;

import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MessageAttachment {

	@JsonProperty("FILE_NAME")
	@Field("FILE_NAME")
	private String fileName;

	@JsonProperty("HASH")
	@Field("HASH")
	public String hash;

	@JsonProperty("FILE")
	@Field("FILE")
	@Transient
	private String file;

	@JsonProperty("FILE_EXTENSION")
	@Field("FILE_EXTENSION")
	@Transient
	private String fileExtension;

	@JsonProperty("ATTACHMENT_UUID")
	@Field("ATTACHMENT_UUID")
	private String attachmentUuid;

	public MessageAttachment() {

	}

	public MessageAttachment(String fileName, String hash, String file, String fileExtension, String attachmentUuid) {
		super();
		this.fileName = fileName;
		this.hash = hash;
		this.file = file;
		this.fileExtension = fileExtension;
		this.attachmentUuid = attachmentUuid;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getHash() {
		return hash;
	}

	public void setHash(String hash) {
		this.hash = hash;
	}

	public String getFile() {
		return file;
	}

	public void setFile(String file) {
		this.file = file;
	}

	public String getFileExtension() {
		return fileExtension;
	}

	public void setFileExtension(String fileExtension) {
		this.fileExtension = fileExtension;
	}

	public String getAttachmentUuid() {
		return attachmentUuid;
	}

	public void setAttachmentUuid(String attachmentUuid) {
		this.attachmentUuid = attachmentUuid;
	}
}
