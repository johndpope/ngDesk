package com.ngdesk.graphql.chat.channel.dao;

import java.util.List;

import org.springframework.data.mongodb.core.mapping.Field;

public class ChatChannelStage {

	@Field("STAGE_ID")
	private String id;

	@Field("NAME")
	private String name;

	@Field("CONDITIONS")
	List<Condition> conditions;

	@Field("NODES")
	List<ChatChannelNode> nodes;

	public ChatChannelStage() {

	}

	public ChatChannelStage(String id, String name, List<Condition> conditions, List<ChatChannelNode> nodes) {
		super();
		this.id = id;
		this.name = name;
		this.conditions = conditions;
		this.nodes = nodes;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<Condition> getConditions() {
		return conditions;
	}

	public void setConditions(List<Condition> conditions) {
		this.conditions = conditions;
	}

	public List<ChatChannelNode> getNodes() {
		return nodes;
	}

	public void setNodes(List<ChatChannelNode> nodes) {
		this.nodes = nodes;
	}

}
