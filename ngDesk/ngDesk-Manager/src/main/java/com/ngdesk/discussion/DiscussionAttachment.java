package com.ngdesk.discussion;

import javax.validation.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(Include.NON_NULL)
public class DiscussionAttachment {

	@JsonProperty("FILE_NAME")
	@NotEmpty(message = "FILE_NAME_REQUIRED")
	private String fileName;

	@JsonProperty("HASH")
	public String hash;

	public DiscussionAttachment() {

	}

	public DiscussionAttachment(@NotEmpty(message = "FILE_NAME_REQUIRED") String fileName, String hash) {
		super();
		this.fileName = fileName;
		this.hash = hash;
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

}
