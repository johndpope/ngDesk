package com.ngdesk.graphql.workflow;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.Pattern;

import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ngdesk.commons.annotations.CustomNotEmpty;
import com.ngdesk.commons.annotations.CustomNotNull;

import io.swagger.v3.oas.annotations.media.Schema;

public class Node {

	@Field("ID")
	private String nodeId;

	@Field("TYPE")
	private String type;

	@Field("CONNECTIONS_TO")
	private List<Connection> connections;

	@Field("NAME")
	private String name;

	@Field("CONDITIONS")
	List<Condition> preConditions;

	public Node(String nodeId, String type, List<Connection> connections, String name, List<Condition> preConditions) {
		super();
		this.nodeId = nodeId;
		this.type = type;
		this.connections = connections;
		this.name = name;
		this.preConditions = preConditions;
	}

	public String getNodeId() {
		return nodeId;
	}

	public void setNodeId(String nodeId) {
		this.nodeId = nodeId;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public List<Connection> getConnections() {
		return connections;
	}

	public void setConnections(List<Connection> connections) {
		this.connections = connections;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<Condition> getPreConditions() {
		return preConditions;
	}

	public void setPreConditions(List<Condition> preConditions) {
		this.preConditions = preConditions;
	}

}
