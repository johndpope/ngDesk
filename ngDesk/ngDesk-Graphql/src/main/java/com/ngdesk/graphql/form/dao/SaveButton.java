package com.ngdesk.graphql.form.dao;

import javax.validation.constraints.Pattern;

public class SaveButton {

	private String label;

	@Pattern(regexp = "center|end|start|stretch", message = "INVALID_ALIGNMENT")
	private String alignment;

	@Pattern(regexp = "message|url", message = "INVALID_SAVE_TYPE")
	private String afterSave;

	private String saveMessage;

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
