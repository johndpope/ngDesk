package com.ngdesk.workflow.dao;

import javax.validation.constraints.Pattern;

import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ngdesk.commons.annotations.CustomNotEmpty;

import io.swagger.v3.oas.annotations.media.Schema;

public class RouteCondition {

	@Schema(required = true, description = "operator for the route node condition")
	@JsonProperty("OPERATOR")
	@Field("OPERATOR")
	@CustomNotEmpty(message = "DAO_VARIABLE_REQUIRED", values = { "OPERATOR" })
	@Pattern(regexp = "''|REGEX|EQUALS_TO|DOES_NOT_CONTAIN|NOT_EQUALS_TO|CONTAINS|DOES_NOT_CONTAIN|LESS_THAN|GREATER_THAN|EXISTS|BETWEEN", message = "NOT_VALID_OPERATOR")
	private String operator;

	@Schema(required = true, description = "value to match")
	@JsonProperty("VALUE")
	@Field("VALUE")
	private String value;

	@Schema(required = true, description = "node id of the next node that it has to route to if the condition is true")
	@JsonProperty("TO_NODE")
	@Field("TO_NODE")
	private String toNode;

	public RouteCondition(
			@Pattern(regexp = "REGEX|EQUALS_TO|NOT_EQUALS_TO|CONTAINS|DOES_NOT_CONTAIN|LESS_THAN|GREATER_THAN|EXISTS|BETWEEN", message = "NOT_VALID_OPERATOR") String operator,
			String value, String toNode) {
		this.operator = operator;
		this.value = value;
		this.toNode = toNode;
	}

	public RouteCondition() {
	}

	public String getOperator() {
		return operator;
	}

	public void setOperator(String operator) {
		this.operator = operator;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getToNode() {
		return toNode;
	}

	public void setToNode(String toNode) {
		this.toNode = toNode;
	}

}
