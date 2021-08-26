package com.ngdesk.channels.facebook;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Entry {
	@JsonProperty("changes")
	private List<Change> changes;

	@JsonProperty("id")
	private String id;

	@JsonProperty("time")
	private long time;

	public Entry() {
	}

	public Entry(List<Change> changes, String id, long time) {
		super();
		this.changes = changes;
		this.id = id;
		this.time = time;
	}

	public List<Change> getChanges() {
		return changes;
	}

	public void setChanges(List<Change> changes) {
		this.changes = changes;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

}
