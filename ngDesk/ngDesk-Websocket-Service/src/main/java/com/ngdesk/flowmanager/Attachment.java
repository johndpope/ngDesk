package com.ngdesk.flowmanager;

import javax.validation.constraints.NotEmpty;

import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(Include.NON_NULL)
public class Attachment {

	@JsonProperty("FILE_NAME")
	@Field("FILE_NAME")
	@NotEmpty(message = "FILE_NAME_REQUIRED")
	private String fileName;

	@JsonProperty("HASH")
	@Field("HASH")
	public String hash;

	@JsonProperty("FILE")
	@Field("FILE")
	private String file;

	@JsonProperty("FILE_EXTENSION")
	@Field("FILE_EXTENSION")
	private String fileExtension;

	@JsonProperty("ATTACHMENT_UUID")
	@Field("ATTACHMENT_UUID")
	private String attachmentUuid;

	public Attachment() {

	}

	public Attachment(@NotEmpty(message = "FILE_NAME_REQUIRED") String fileName, String hash, String file,
			String fileExtension, String attachmentUuid) {
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
