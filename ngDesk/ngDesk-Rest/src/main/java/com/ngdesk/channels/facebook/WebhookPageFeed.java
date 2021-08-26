package com.ngdesk.channels.facebook;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class WebhookPageFeed {

	@JsonProperty("entry")
	private List<Entry> entry;

	@JsonProperty("object")
	private String object;

	public WebhookPageFeed() {
	}

	public WebhookPageFeed(List<Entry> entry, String object) {
		super();
		this.entry = entry;
		this.object = object;
	}

	public List<Entry> getEntry() {
		return entry;
	}

	public void setEntry(List<Entry> entry) {
		this.entry = entry;
	}

	public String getObject() {
		return object;
	}

	public void setObject(String object) {
		this.object = object;
	}

}
