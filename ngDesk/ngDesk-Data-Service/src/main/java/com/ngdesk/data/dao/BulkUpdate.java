package com.ngdesk.data.dao;

import java.util.List;
import java.util.Map;

import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ngdesk.commons.annotations.CustomNotEmpty;
import com.ngdesk.commons.annotations.CustomNotNull;

public class BulkUpdate {

	@JsonProperty("ENTRY_IDS")
	@CustomNotNull(message = "NOT_NULL", values = { "ENTRY_ID" })
	@Size(min = 1, message = "ATLEAST_ONE_ENTRY_ID_REQUIRED")
	private List<String> entryIds;

	@JsonProperty("UPDATE")
	@CustomNotNull(message = "NOT_NULL", values = { "UPDATE" })
	private Map<String, Object> update;

	public BulkUpdate() {
	}

	public BulkUpdate(@Size(min = 1, message = "ATLEAST_ONE_ENTRY_ID_REQUIRED") List<String> entryIds,
			Map<String, Object> update) {
		this.entryIds = entryIds;
		this.update = update;
	}

	public List<String> getEntryIds() {
		return entryIds;
	}

	public void setEntryIds(List<String> entryIds) {
		this.entryIds = entryIds;
	}

	public Map<String, Object> getUpdate() {
		return update;
	}

	public void setUpdate(Map<String, Object> update) {
		this.update = update;
	}

}
