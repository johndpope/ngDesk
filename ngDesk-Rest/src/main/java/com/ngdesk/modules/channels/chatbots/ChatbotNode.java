package com.ngdesk.modules.channels.chatbots;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ngdesk.annotations.NodeType;

public class ChatbotNode {
	@JsonProperty("POSITION_X")
	@NotNull(message = "NODE_POSITION_X_NULL")
	private int positionX;

	@JsonProperty("POSITION_Y")
	@NotNull(message = "NODE_POSITION_Y_NULL")
	private int positionY;

	@JsonProperty("VALUES")
	@NotNull(message = "NODE_VALUES_NULL")
	@Valid
	private ChatbotValue values;

	@JsonProperty("ID")
	@NotNull(message = "NODE_ID_NULL")
	@Size(min = 1, message = "NODE_ID_BLANK")
	private String nodeId;

	@JsonProperty("TYPE")
	@NotNull(message = "NODE_TYPE_NULL")
	@NodeType
	private String type;

	@JsonProperty("CONNECTS_TO")
	@NotNull(message = "CONNECTIONS_TO_NULL")
	@Valid
	private String connections;

	@JsonProperty("NAME")
	@NotNull(message = "NODE_NAME_NULL")
	@Size(min = 1, message = "NO_NODE_NAME")
	private String name;

	@JsonProperty("SUB_TYPE")
	@NotNull(message = "NODE_SUB_TYPE_NULL")
	@Size(min = 1, message = "NO_NODE_SUB_TYPE")
	private String subType;

	public ChatbotNode() {

	}

	public ChatbotNode(@NotNull(message = "NODE_POSITION_X_NULL") int positionX,
			@NotNull(message = "NODE_POSITION_Y_NULL") int positionY,
			@NotNull(message = "NODE_VALUES_NULL") @Valid ChatbotValue values,
			@NotNull(message = "NODE_ID_NULL") @Size(min = 1, message = "NODE_ID_BLANK") String nodeId,
			@NotNull(message = "NODE_TYPE_NULL") String type,
			@NotNull(message = "CONNECTIONS_TO_NULL") @Valid String connections,
			@NotNull(message = "NODE_NAME_NULL") @Size(min = 1, message = "NO_NODE_NAME") String name,
			@NotNull(message = "NODE_SUB_TYPE_NULL") @Size(min = 1, message = "NO_NODE_SUB_TYPE") String subType) {
		super();
		this.positionX = positionX;
		this.positionY = positionY;
		this.values = values;
		this.nodeId = nodeId;
		this.type = type;
		this.connections = connections;
		this.name = name;
		this.subType = subType;
	}

	public int getPositionX() {
		return positionX;
	}

	public void setPositionX(int positionX) {
		this.positionX = positionX;
	}

	public int getPositionY() {
		return positionY;
	}

	public void setPositionY(int positionY) {
		this.positionY = positionY;
	}

	public ChatbotValue getValues() {
		return values;
	}

	public void setValues(ChatbotValue values) {
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

	public String getConnections() {
		return connections;
	}

	public void setConnections(String connections) {
		this.connections = connections;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSubType() {
		return subType;
	}

	public void setSubType(String subType) {
		this.subType = subType;
	}

}
