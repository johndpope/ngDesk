package com.ngdesk.integration.zoom.dao;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MeetingRequest {

	@JsonProperty("ENTRY_ID")
	private String entryId;

	@JsonProperty("MODULE_ID")
	private String moduleId;

	@JsonProperty("TOPIC")
	private String topic;

	public MeetingRequest() {
		super();
	}

	public MeetingRequest(String entryId, String moduleId, String topic) {
		super();
		this.entryId = entryId;
		this.moduleId = moduleId;
		this.topic = topic;
	}

	public String getEntryId() {
		return entryId;
	}

	public void setEntryId(String entryId) {
		this.entryId = entryId;
	}

	public String getModuleId() {
		return moduleId;
	}

	public void setModuleId(String moduleId) {
		this.moduleId = moduleId;
	}

	public String getTopic() {
		return topic;
	}

	public void setTopic(String topic) {
		this.topic = topic;
	}

}
