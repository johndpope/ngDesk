package com.ngdesk;

import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Queue {

	@JsonProperty("QUEUE_ID")
	private String queueId;

	@JsonProperty("TEAMS")
	private List<String> teamids;

	public String getQueueId() {
		return queueId;
	}

	public void setQueueId(String queueId) {
		this.queueId = queueId;
	}

	public List<String> getTeamids() {
		return teamids;
	}

	public void setTeamids(List<String> teamids) {
		this.teamids = teamids;
	}

}
