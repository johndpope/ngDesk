package com.ngdesk.publicticket;

import javax.validation.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(Include.NON_NULL)
public class PublicTicketAttachment {

	@JsonProperty("FILE_NAME")
	@NotEmpty(message = "FILE_NAME_REQUIRED")
	private String fileName;

	@JsonProperty("HASH")
	public String hash;

	@JsonProperty("FILE")
	@NotEmpty(message = "FILE_REQUIRED")
	private String file;

	@JsonProperty("FILE_EXTENSION")
	private String fileExtension;

	@JsonProperty("ATTACHMENT_UUID")
	private String attachmentUuid;

	public PublicTicketAttachment() {

	}

	public PublicTicketAttachment(@NotEmpty(message = "FILE_NAME_REQUIRED") String fileName, String hash, String file,
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
