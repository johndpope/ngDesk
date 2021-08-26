package com.ngdesk.integration.zoom.dao;

import java.util.Date;

import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ZoomMeeting {

	@JsonProperty("MEETING_ID")
	@Field("MEETING_ID")
	private Long meetingId;

	@JsonProperty("JOIN_URL")
	@Field("JOIN_URL")
	private String joinUrl;

	@JsonProperty("MEETING_START_URL")
	@Field("MEETING_START_URL")
	private String meetingStartUrl;

	@JsonProperty("TOPIC")
	@Field("TOPIC")
	private String topic;

	@JsonProperty("ENTRY_ID")
	@Field("ENTRY_ID")
	private String entryId;

	@JsonProperty("MODULE_ID")
	@Field("MODULE_ID")
	private String moduleId;

	@JsonProperty("DATE_CREATED")
	@Field("DATE_CREATED")
	private Date dateCreated;

	@JsonProperty("CREATED_BY")
	@Field("CREATED_BY")
	private String createdBy;

	public ZoomMeeting(Long meetingId, String joinUrl, String meetingStartUrl, String topic, String entryId,
			String moduleId, Date dateCreated, String createdBy) {
		super();
		this.meetingId = meetingId;
		this.joinUrl = joinUrl;
		this.meetingStartUrl = meetingStartUrl;
		this.topic = topic;
		this.entryId = entryId;
		this.moduleId = moduleId;
		this.dateCreated = dateCreated;
		this.createdBy = createdBy;
	}

	public ZoomMeeting() {
		super();
	}

	public Long getMeetingId() {
		return meetingId;
	}

	public void setMeetingId(Long meetingId) {
		this.meetingId = meetingId;
	}

	public String getJoinUrl() {
		return joinUrl;
	}

	public void setJoinUrl(String joinUrl) {
		this.joinUrl = joinUrl;
	}

	public String getMeetingStartUrl() {
		return meetingStartUrl;
	}

	public void setMeetingStartUrl(String meetingStartUrl) {
		this.meetingStartUrl = meetingStartUrl;
	}

	public String getTopic() {
		return topic;
	}

	public void setTopic(String topic) {
		this.topic = topic;
	}

	public Date getDateCreated() {
		return dateCreated;
	}

	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
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

}
