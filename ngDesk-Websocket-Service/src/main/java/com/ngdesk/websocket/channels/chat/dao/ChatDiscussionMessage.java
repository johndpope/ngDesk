package com.ngdesk.websocket.channels.chat.dao;

import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ngdesk.data.dao.DiscussionMessage;

public class ChatDiscussionMessage {

	@JsonProperty("AGENT_DATA_ID")
	@Field("AGENT_DATA_ID")
	private String agentDataID;

	@JsonProperty("SESSION_UUID")
	@Field("SESSION_UUID")
	private String sessionUuid;

	@JsonProperty("DISCUSSION_MESSAGE")
	@Field("DISCUSSION_MESSAGE")
	private DiscussionMessage DiscussionMessage;

	public ChatDiscussionMessage() {

	}

	public ChatDiscussionMessage(String agentDataID, String sessionUuid, DiscussionMessage discussionMessage) {
		super();
		this.agentDataID = agentDataID;
		this.sessionUuid = sessionUuid;
		DiscussionMessage = discussionMessage;
	}

	public String getAgentDataID() {
		return agentDataID;
	}

	public void setAgentDataID(String agentDataID) {
		this.agentDataID = agentDataID;
	}

	public String getSessionUuid() {
		return sessionUuid;
	}

	public void setSessionUuid(String sessionUuid) {
		this.sessionUuid = sessionUuid;
	}

	public DiscussionMessage getDiscussionMessage() {
		return DiscussionMessage;
	}

	public void setDiscussionMessage(DiscussionMessage discussionMessage) {
		DiscussionMessage = discussionMessage;
	}

}
