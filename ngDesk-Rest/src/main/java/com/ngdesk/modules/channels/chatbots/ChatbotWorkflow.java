package com.ngdesk.modules.channels.chatbots;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ngdesk.annotations.ValidWorkflow;

public class ChatbotWorkflow {
	@JsonProperty("NODES")
	@NotNull(message = "NODES_NOT_NULL")
	@Valid
	private List<ChatbotNode> nodes;

	public ChatbotWorkflow() {

	}

	public ChatbotWorkflow(@NotNull(message = "NODES_NOT_NULL") @Valid List<ChatbotNode> nodes) {
		super();
		this.nodes = nodes;
	}

	public List<ChatbotNode> getNodes() {
		return nodes;
	}

	public void setNodes(List<ChatbotNode> nodes) {
		this.nodes = nodes;
	}

}
