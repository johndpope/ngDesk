package com.ngdesk.modules;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UpdateData {

	@JsonProperty("IDS")
	@NotEmpty(message = "IDS_REQUIRED")
	List<String> ids;

	@JsonProperty("ENTRIES")
	@NotEmpty(message = "ENTRIES_REQUIRED")
	Map<String, String> entries = new HashMap<String, String>();

	public UpdateData() {

	}

	public UpdateData(@NotEmpty(message = "IDS_REQUIRED") List<String> ids) {
		super();
		this.ids = ids;
	}

	public List<String> getIds() {
		return ids;
	}

	public void setIds(List<String> ids) {
		this.ids = ids;
	}

	public Map getEntries() {
		return entries;
	}

	public void setEntries(Map entries) {
		this.entries = entries;
	}

}
