package com.ngdesk.modules;

import java.util.List;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

public class GetData {
	@JsonProperty("IDS")
	@NotNull(message = "IDS_REQUIRED")
	List<String> ids;

	public GetData() {

	}

	public GetData(@NotNull(message = "IDS_REQUIRED") List<String> ids) {
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
