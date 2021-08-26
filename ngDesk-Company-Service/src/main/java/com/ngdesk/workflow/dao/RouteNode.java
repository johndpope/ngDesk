package com.ngdesk.workflow.dao;

import java.util.List;

import javax.validation.Valid;

import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RouteNode extends Node {

	@JsonProperty("VARIABLE")
	@Field("VARIABLE")
	private String variable;

	@JsonProperty("ROUTE_CONDITIONS")
	@Field("ROUTE_CONDITIONS")
	private List<RouteCondition> conditions;

	public String getVariable() {
		return variable;
	}

	public void setVariable(String variable) {
		this.variable = variable;
	}

	public List<RouteCondition> getConditions() {
		return conditions;
	}

	public void setConditions(List<RouteCondition> conditions) {
		this.conditions = conditions;
	}

	public RouteNode() {
	}

	public RouteNode(String variable, @Valid List<RouteCondition> conditions) {
		this.variable = variable;
		this.conditions = conditions;
	}

}
