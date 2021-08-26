package com.ngdesk.knowledgebase.article.dao;

import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.AccessMode;

public class Attachment {
	
	@Schema(description = "Attachment uuid")
	private String attachmentUuid;

	@Schema(description = "file")
	private String file;
	
	@Schema(description = "hash")
	private String hash;
		
	@Schema(description = "file name")
	private String fileName;
	
	public Attachment() {}
	
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
