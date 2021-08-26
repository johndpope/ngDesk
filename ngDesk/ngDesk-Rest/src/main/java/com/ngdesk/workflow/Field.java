package com.ngdesk.workflow;

import java.util.List;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ngdesk.annotations.ValidOperator;

public class Field {

	@JsonProperty("FIELD")
	@NotNull(message = "FIELD_ID_NULL")
	@Size(min = 1, message = "FIELD_ID_BLANK")
	private String fieldId;

	@JsonProperty("VALUE")
	@Size(min = 1, message = "FIELD_VALUE_BLANK")
	private List<String> value;

	@JsonProperty("ATTACHMENTS")
	private String attachment;

	@JsonProperty("OPERATOR")
	@ValidOperator
	private String operator;

	public Field() {

	}

	public Field(@NotNull(message = "FIELD_ID_NULL") @Size(min = 1, message = "FIELD_ID_BLANK") String fieldId,
			List<String> value, String attachment, String operator) {
		super();
		this.fieldId = fieldId;
		this.value = value;
		this.attachment = attachment;
		this.operator = operator;
	}

	public String getFieldId() {
		return fieldId;
	}

	public void setFieldId(String fieldId) {
		this.fieldId = fieldId;
	}

	public List<String> getValue() {
		return value;
	}

	public void setValue(List<String> value) {
		this.value = value;
	}

	public String getAttachment() {
		return attachment;
	}

	public void setAttachment(String attachment) {
		this.attachment = attachment;
	}

	public String getOperator() {
		return operator;
	}

	public void setOperator(String operator) {
		this.operator = operator;
	}

}
