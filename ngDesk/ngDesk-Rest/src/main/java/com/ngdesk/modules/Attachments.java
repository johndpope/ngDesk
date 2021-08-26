package com.ngdesk.modules;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Attachments {

	@JsonProperty("ATTACHMENTS")
	@NotNull(message = "ATTACHMENTS_KEY_MISSING")
	@Size(min = 1, message = "ATTACHMENT_REQUIRED")
	@Valid
	public List<Attachment> attachments;

	public Attachments() {

	}

	public Attachments(
			@NotNull(message = "ATTACHMENTS_KEY_MISSING") @Size(min = 1, message = "ATTACHMENT_REQUIRED") @Valid List<Attachment> attachments) {
		super();
		this.attachments = attachments;
	}

	public List<Attachment> getAttachments() {
		return attachments;
	}

	public void setAttachments(List<Attachment> attachments) {
		this.attachments = attachments;
	}

}
