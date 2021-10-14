package com.ngdesk.websocket.channels.chat.dao;

import com.ngdesk.data.dao.DiscussionMessage;

public class ChatDiscussionMessage {

	private String agentDataID;

	private String customerDataID;

	private String sessionUUID;

	private DiscussionMessage discussionMessage;

	public ChatDiscussionMessage() {

	}

	public ChatDiscussionMessage(String agentDataID, String customerDataID, String sessionUUID,
			DiscussionMessage discussionMessage) {
		super();
		this.agentDataID = agentDataID;
		this.customerDataID = customerDataID;
		this.sessionUUID = sessionUUID;
		this.discussionMessage = discussionMessage;
	}

	public String getAgentDataID() {
		return agentDataID;
	}

	public void setAgentDataID(String agentDataID) {
		this.agentDataID = agentDataID;
	}

	public String getCustomerDataID() {
		return customerDataID;
	}

	public void setCustomerDataID(String customerDataID) {
		this.customerDataID = customerDataID;
	}

	public String getSessionUUID() {
		return sessionUUID;
	}

	public void setSessionUUID(String sessionUUID) {
		this.sessionUUID = sessionUUID;
	}

	public DiscussionMessage getDiscussionMessage() {
		return discussionMessage;
	}

	public void setDiscussionMessage(DiscussionMessage discussionMessage) {
		this.discussionMessage = discussionMessage;
	}

}
