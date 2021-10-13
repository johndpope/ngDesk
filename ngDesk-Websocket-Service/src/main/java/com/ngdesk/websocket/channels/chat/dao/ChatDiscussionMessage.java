package com.ngdesk.websocket.channels.chat.dao;

import com.ngdesk.data.dao.DiscussionMessage;

public class ChatDiscussionMessage {

	private String agentDataID;

	private String customerDataID;

	private String sessionUuid;

	private DiscussionMessage DiscussionMessage;

	public ChatDiscussionMessage() {

	}

	public ChatDiscussionMessage(String agentDataID, String customerDataID, String sessionUuid,
			DiscussionMessage discussionMessage) {
		super();
		this.agentDataID = agentDataID;
		this.customerDataID = customerDataID;
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

	public String getCustomerDataID() {
		return customerDataID;
	}

	public void setCustomerDataID(String customerDataID) {
		this.customerDataID = customerDataID;
	}

}