package com.ngdesk.modules.forms;

import javax.validation.constraints.Pattern;

import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SaveButton {

	@JsonProperty("LABEL")
	@Field("LABEL")
	private String label;

	@JsonProperty("ALIGNMENT")
	@Field("ALIGNMENT")
	@Pattern(regexp = "center|end|start|stretch", message = "INVALID_ALIGNMENT")
	private String alignment;

	@JsonProperty("SAVE_TYPE")
	@Field("SAVE_TYPE")
	@Pattern(regexp = "message|url", message = "INVALID_SAVE_TYPE")
	private String afterSave;

	@JsonProperty("SAVE_MESSAGE")
	@Field("SAVE_MESSAGE")
	private String saveMessage;

	@JsonProperty("URL")
	@Field("URL")
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
