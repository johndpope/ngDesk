package com.ngdesk.data.dao;

import java.util.List;

import javax.validation.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ngdesk.commons.annotations.CustomNotEmpty;

public class DeleteData {

	@JsonProperty("IDS")
	@CustomNotEmpty(message = "IDS_REQUIRED", values = {})
	private List<String> ids;

	public DeleteData() {

	}

	public DeleteData(@NotEmpty(message = "IDS_REQUIRED") List<String> ids) {
		super();
		this.ids = ids;
	}

	public List<String> getIds() {
		return ids;
	}

	public void setIds(List<String> ids) {
		this.ids = ids;
	}

}