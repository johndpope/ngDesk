package com.ngdesk.workflow;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ngdesk.annotations.NodeType;
import com.ngdesk.annotations.ValidNode;

@ValidNode
public class Node {

	@JsonProperty("POSITION_X")
	@NotNull(message = "NODE_POSITION_X_NULL")
	@Size(min = 1, message = "NO_NODE_POSITION_X")
	private String positionX;

	@JsonProperty("POSITION_Y")
	@NotNull(message = "NODE_POSITION_Y_NULL")
	@Size(min = 1, message = "NO_NODE_POSITION_Y")
	private String positionY;

	@JsonProperty("VALUES")
	@NotNull(message = "NODE_VALUES_NULL")
	@Valid
	private Values values;

	@JsonProperty("ID")
	@NotNull(message = "NODE_ID_NULL")
	@Size(min = 1, message = "NODE_ID_BLANK")
	private String nodeId;

	@JsonProperty("TYPE")
	@NotNull(message = "NODE_TYPE_NULL")
	@NodeType
	private String type;

	@JsonProperty("CONNECTIONS_TO")
	@NotNull(message = "CONNECTIONS_TO_NULL")
	@Valid
	private List<Connection> connections;

	@JsonProperty("NAME")
	@NotNull(message = "NODE_NAME_NULL")
	@Size(min = 1, message = "NO_NODE_NAME")
	private String name;

	@JsonProperty("PLUGS")
	@NotNull(message = "PLUGS_NULL")
	@Valid
	private List<Plug> plugs;

	public Node() {

	}

	public Node(
			@NotNull(message = "NODE_POSITION_X_NULL") @Size(min = 1, message = "NO_NODE_POSITION_X") String positionX,
			@NotNull(message = "NODE_POSITION_Y_NULL") @Size(min = 1, message = "NO_NODE_POSITION_Y") String positionY,
			@NotNull(message = "NODE_VALUES_NULL") @Valid Values values,
			@NotNull(message = "NODE_ID_NULL") @Size(min = 1, message = "NODE_ID_BLANK") String nodeId,
			@NotNull(message = "NODE_TYPE_NULL") String type,
			@NotNull(message = "CONNECTIONS_TO_NULL") @Valid List<Connection> connections,
			@NotNull(message = "NODE_NAME_NULL") @Size(min = 1, message = "NO_NODE_NAME") String name,
			@NotNull(message = "PLUGS_NULL") @Valid List<Plug> plugs) {
		super();
		this.positionX = positionX;
		this.positionY = positionY;
		this.values = values;
		this.nodeId = nodeId;
		this.type = type;
		this.connections = connections;
		this.name = name;
		this.plugs = plugs;
	}

	public String getPositionX() {
		return positionX;
	}

	public void setPositionX(String positionX) {
		this.positionX = positionX;
	}

	public String getPositionY() {
		return positionY;
	}

	public void setPositionY(String positionY) {
		this.positionY = positionY;
	}

	public Values getValues() {
		return values;
	}

	public void setValues(Values values) {
		this.values = values;
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

	public List<Plug> getPlugs() {
		return plugs;
	}

	public void setPlugs(List<Plug> plugs) {
		this.plugs = plugs;
	}

}
