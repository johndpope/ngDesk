package com.ngdesk.workflow.dao;

import java.util.List;

import javax.validation.Valid;

import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;

public class MicrosoftTeamsNotificationNode extends Node{

	
	@Schema(required = true, description = "channel id is required")
	@JsonProperty("CHANNEL_ID")
	@Field("CHANNEL_ID")
	private String channelId;

	@Schema(required = true, description = "field id is required")
	@JsonProperty("FIELDS")
	@Field("FIELD_ID")
	@Valid
	private List<String> fieldIds;

	public MicrosoftTeamsNotificationNode() {

	}

	public MicrosoftTeamsNotificationNode(String channelId, @Valid List<String> fieldIds) {
		super();
		this.channelId = channelId;
		this.fieldIds = fieldIds;
	}

	public String getChannelId() {
		return channelId;
	}

	public void setChannelId(String channelId) {
		this.channelId = channelId;
	}

	public List<String> getFieldIds() {
		return fieldIds;
	}

	public void setFieldIds(List<String> fieldIds) {
		this.fieldIds = fieldIds;
	}

}
