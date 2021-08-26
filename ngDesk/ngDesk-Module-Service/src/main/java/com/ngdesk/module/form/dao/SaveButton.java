package com.ngdesk.module.form.dao;

import javax.validation.constraints.Pattern;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.AccessMode;

public class SaveButton {

	@Schema(description = "Label", required = false)
	private String label;

	@Schema(description = "Alignment", required = true)
	@Pattern(regexp = "center|end|start|stretch", message = "INVALID_ALIGNMENT")
	private String alignment;

	@Schema(description = "After Save", required = false)
	@Pattern(regexp = "message|url", message = "INVALID_SAVE_TYPE")
	private String afterSave;

	@Schema(description = "Save Message", required = false)
	private String saveMessage;

	@Schema(description = "URL")
	private String url;

	public SaveButton() {

	}

	public SaveButton(String label,
			@Pattern(regexp = "center|end|start|stretch", message = "INVALID_ALIGNMENT") String alignment,
			@Pattern(regexp = "message|url", message = "INVALID_SAVE_TYPE") String afterSave, String saveMessage,
			String url) {
		super();
		this.label = label;
		this.alignment = alignment;
		this.afterSave = afterSave;
		this.saveMessage = saveMessage;
		this.url = url;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getAlignment() {
		return alignment;
	}

	public void setAlignment(String alignment) {
		this.alignment = alignment;
	}

	public String getAfterSave() {
		return afterSave;
	}

	public void setAfterSave(String afterSave) {
		this.afterSave = afterSave;
	}

	public String getSaveMessage() {
		return saveMessage;
	}

	public void setSaveMessage(String saveMessage) {
		this.saveMessage = saveMessage;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

}
