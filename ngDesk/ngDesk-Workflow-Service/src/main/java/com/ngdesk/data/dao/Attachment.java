package com.ngdesk.data.dao;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Attachment {

	@JsonProperty("ATTACHMENT_ID")
	@Id
	private String attachmentId;

	@JsonProperty("HASH")
	@Field("HASH")
	public String hash;

	@JsonProperty("FILE")
	@Field("FILE")
	private String file;

	@JsonProperty("ATTACHMENT_UUID")
	@Field("ATTACHMENT_UUID")
	private String attachmentUuid;

	public Attachment() {

	}

	public Attachment(String attachmentId, String hash, String file, String attachmentUuid) {
		super();
		this.attachmentId = attachmentId;
		this.hash = hash;
		this.file = file;
		this.attachmentUuid = attachmentUuid;
	}

	public String getAttachmentId() {
		return attachmentId;
	}

	public void setAttachmentId(String attachmentId) {
		this.attachmentId = attachmentId;
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

	public String getAttachmentUuid() {
		return attachmentUuid;
	}

	public void setAttachmentUuid(String attachmentUuid) {
		this.attachmentUuid = attachmentUuid;
	}

}
