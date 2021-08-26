package com.ngdesk.websocket.workflow.dao;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Stage {

	@JsonProperty("STAGE_ID")
	private String id;

	@JsonProperty("NAME")
	private String name;

	@JsonProperty("CONDITIONS")
	List<Condition> conditions;

	@JsonProperty("NODES")
	List<Node> nodes;

	public Stage() {

	}

	public Stage(String id, String name, List<Condition> conditions, List<Node> nodes) {
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

	public List<Node> getNodes() {
		return nodes;
	}

	public void setNodes(List<Node> nodes) {
		this.nodes = nodes;
	}

}
