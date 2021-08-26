package com.ngdesk.workflow.dao;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.Pattern;

import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "TYPE", visible = true)
@JsonSubTypes({ @JsonSubTypes.Type(value = SendEmailNode.class, name = "SendEmail"),
		@JsonSubTypes.Type(value = SendSmsNode.class, name = "SendSms"),
		@JsonSubTypes.Type(value = StartNode.class, name = "Start"),
		@JsonSubTypes.Type(value = CreateEntryNode.class, name = "CreateEntry"),
		@JsonSubTypes.Type(value = UpdateEntryNode.class, name = "UpdateEntry"),
		@JsonSubTypes.Type(value = StartEscalationNode.class, name = "StartEscalation"),
		@JsonSubTypes.Type(value = StopEscalationNode.class, name = "StopEscalation"),
		@JsonSubTypes.Type(value = MakePhoneCallNode.class, name = "MakePhoneCall"),
		@JsonSubTypes.Type(value = ApprovalNode.class, name = "Approval"),
		@JsonSubTypes.Type(value = JavascriptNode.class, name = "Javascript"),
		@JsonSubTypes.Type(value = RouteNode.class, name = "Route"),
		@JsonSubTypes.Type(value = EndNode.class, name = "End"),
		@JsonSubTypes.Type(value = DeleteEntryNode.class, name = "DeleteEntry"),
        @JsonSubTypes.Type(value = NotifyProbeNode.class, name = "NotifyProbe") })

public abstract class Node {

	@JsonProperty("ID")
	@Field("ID")
	private String nodeId;

	@JsonProperty("TYPE")
	@Field("TYPE")
	private String type;

	@JsonProperty("CONNECTIONS_TO")
	@Field("CONNECTIONS_TO")
	private List<Connection> connections;

	@JsonProperty("NAME")
	@Field("NAME")
	private String name;

	@JsonProperty("CONDITIONS")
	@Field("CONDITIONS")
	List<Condition> preConditions;

	public Node() {

	}

	public Node(String nodeId,
			@Pattern(regexp = "Route|CreateEntry|UpdateEntry|Javascript|HttpRequest|SendEmail|DeleteEntry|Start|StartEscalation|StopEscalation|MakePhoneCall|Approval|SendSms|FindAgentAndAssign|ChatBot|NotifyProbe", message = "INVALID_NODE_TYPE") String type,
			@Valid List<Connection> connections, String name, @Valid List<Condition> preConditions) {
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
