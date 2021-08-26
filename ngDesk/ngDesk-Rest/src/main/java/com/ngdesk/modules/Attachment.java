package com.ngdesk.modules;

import javax.validation.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(Include.NON_NULL)
public class Attachment {

	@JsonProperty("FILE")
	private String file;

	@JsonProperty("HASH")
	public String hash;

	@JsonProperty("ATTACHMENT_UUID")
	public String attachmentUuid;

	@JsonProperty("ATTACHMENT_ID")
	public String attachmentId;

	@JsonProperty("FILE_NAME")
	@NotEmpty(message = "FILE_NAME_REQUIRED")
	public String filename;

	public Attachment() {

	}

	public Attachment(@NotEmpty(message = "ATTACHMENT_FILE_REQUIRED") String file, String hash, String attachmentUuid,
			String attachmentId, String filename) {
		super();
		this.file = file;
		this.hash = hash;
		this.attachmentUuid = attachmentUuid;
		this.attachmentId = attachmentId;
		this.filename = filename;
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

	public String getAttachmentId() {
		return attachmentId;
	}

	public void setAttachmentId(String attachmentId) {
		this.attachmentId = attachmentId;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

}
