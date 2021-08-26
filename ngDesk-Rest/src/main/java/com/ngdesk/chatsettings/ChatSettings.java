package com.ngdesk.chatsettings;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ChatSettings {

	@JsonProperty("MAX_CHATS_PER_AGENT")
	@Min(value = 0, message = "INVALID_MAX_CHATS_PER_AGENT")
	@Max(value = 50, message = "MAX_CHATS_REACHED")
	public int maxChatsPerAgent;

	public ChatSettings() {

	}

	public ChatSettings(
			@Min(value = 0, message = "INVALID_MAX_CHATS_PER_AGENT") @Max(value = 50, message = "MAX_CHATS_REACHED") int maxChatsPerAgent) {
		super();
		this.maxChatsPerAgent = maxChatsPerAgent;
	}

	public int getMaxChatsPerAgent() {
		return maxChatsPerAgent;
	}

	public void setMaxChatsPerAgent(int maxChatsPerAgent) {
		this.maxChatsPerAgent = maxChatsPerAgent;
	}

}
