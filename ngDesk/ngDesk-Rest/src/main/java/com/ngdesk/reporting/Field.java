package com.ngdesk.reporting;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Field {

	@JsonProperty("FIELD_ID")
	@NotNull(message = "FIELD_ID_NOT_NULL")
	@Size(min = 1, message = "FIELD_ID_EMPTY")
	private String id;

	@JsonProperty("DATA")
	private List<String> data;

	public Field() {

	}

	public Field(@NotNull(message = "FIELD_ID_NOT_NULL") @Size(min = 1, message = "FIELD_ID_EMPTY") String id,
			List<String> data) {
		super();
		this.id = id;
		this.data = data;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public List<String> getData() {
		return data;
	}

	public void setData(List<String> data) {
		this.data = data;
	}

}
