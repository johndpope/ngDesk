package com.ngdesk.workflow;

import java.util.List;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Condition {
	@JsonProperty("ORDER")
	@NotNull(message = "ORDER_NULL")
	private int order;

	@JsonProperty("VALUE")
//	NOW VALIDATED IN WORKFLOW SERVICES
//	@NotNull(message="VALUE_NULL")
//	@Size(min=1, message="value_blank")
	private String value;

	@JsonProperty("VALUES")
//	FOR ROUTE NODE
//	NOW VALIDATED IN WORKFLOW SERVICES
	private List<String> values;

	@JsonProperty("TO_NODE")
	// @NotNull(message="CONDITION_TO_NODE_NULL")
	// @Size(min=1, message="CONDITION_TO_NODE_BLANK")
	private String toNode;

	@JsonProperty("OPERATOR")
	@NotNull(message = "CONDITION_OPERATOR_NULL")
//	@Size(min=1, message="CONDITION_OPERATOR_BLANK")
	private String operator;

	public Condition() {

	}

	public Condition(@NotNull(message = "ORDER_NULL") int order, String value, List<String> values, String toNode,
			@NotNull(message = "CONDITION_OPERATOR_NULL") String operator) {
		super();
		this.order = order;
		this.value = value;
		this.values = values;
		this.toNode = toNode;
		this.operator = operator;
	}

	public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		this.order = order;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public List<String> getValues() {
		return values;
	}

	public void setValues(List<String> values) {
		this.values = values;
	}

	public String getToNode() {
		return toNode;
	}

	public void setToNode(String toNode) {
		this.toNode = toNode;
	}

	public String getOperator() {
		return operator;
	}

	public void setOperator(String operator) {
		this.operator = operator;
	}

}
