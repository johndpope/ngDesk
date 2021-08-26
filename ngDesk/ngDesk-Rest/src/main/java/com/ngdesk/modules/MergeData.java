package com.ngdesk.modules;

import java.util.List;

import javax.validation.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MergeData {

	@JsonProperty("ENTRY")
	@NotEmpty(message = "ENTRY_REQUIRED")
	private String entry;

	@JsonProperty("MERGE_ENTRIES")
	@NotEmpty(message = "MERGE_ENTRIES_REQUIRED")
	private List<String> mergeEntries;
	
	public MergeData() {}

	public MergeData(@NotEmpty(message = "ENTRY_REQUIRED") String entry,
			@NotEmpty(message = "MERGE_ENTRIES_REQUIRED") List<String> mergeEntries) {
		super();
		this.entry = entry;
		this.mergeEntries = mergeEntries;
	}

	public String getEntry() {
		return entry;
	}

	public void setEntry(String entry) {
		this.entry = entry;
	}

	public List<String> getMergeEntries() {
		return mergeEntries;
	}

	public void setMergeEntries(List<String> mergeEntries) {
		this.mergeEntries = mergeEntries;
	}

}
