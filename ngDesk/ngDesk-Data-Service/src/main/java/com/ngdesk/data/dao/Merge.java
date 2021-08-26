package com.ngdesk.data.dao;

import java.util.List;

import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ngdesk.commons.annotations.CustomNotEmpty;
import com.ngdesk.commons.annotations.CustomNotNull;

public class Merge {

	@JsonProperty("ENTRY_ID")
	@CustomNotEmpty(message = "DAO_VARIABLE_REQUIRED", values = { "ENTRY_ID" })
	private String entryId;

	@JsonProperty("MERGE_ENTRY_IDS")
	@CustomNotNull(message = "NOT_NULL", values = { "MERGE_ENTRY_IDS" })
	@Size(min = 1, message = "ATLEAST_ONE_ENTRY_ID_REQUIRED")
	private List<String> mergeEntryIds;

	public Merge(String entryId, @Size(min = 1, message = "ATLEAST_ONE_ENTRY_ID_REQUIRED") List<String> mergeEntryIds) {
		this.entryId = entryId;
		this.mergeEntryIds = mergeEntryIds;
	}

	public Merge() {
	}

	public String getEntryId() {
		return entryId;
	}

	public void setEntryId(String entryId) {
		this.entryId = entryId;
	}

	public List<String> getMergeEntryIds() {
		return mergeEntryIds;
	}

	public void setMergeEntryIds(List<String> mergeEntryIds) {
		this.mergeEntryIds = mergeEntryIds;
	}

}
