package com.ngdesk.modules.rules;

import java.sql.Timestamp;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Rule {

	@JsonProperty("NAME")
	@NotNull(message = "RULE_NAME_NOT_NULL")
	@Size(min = 1, message = "RULE_NAME_NOT_EMPTY")
	private String name;

	@JsonProperty("CONDITIONS")
	@NotNull(message = "CONDITIONS_NOT_NULL")
	@Size(min = 1, message = "CONDITIONS_NOT_EMPTY")
	@Valid
	private List<Condition> conditions;

	@JsonProperty("ACTIONS")
	@NotNull(message = "ACTIONS_NOT_NULL")
	@Size(min = 1, message = "ACTIONS_NOT_EMPTY")
	@Valid
	private List<Action> actions;

	public Rule() {

	}

	public Rule(@NotNull(message = "RULE_NAME_NOT_NULL") @Size(min = 1, message = "RULE_NAME_NOT_EMPTY") String name,
			@NotNull(message = "CONDITIONS_NOT_NULL") @Size(min = 1, message = "CONDITIONS_NOT_EMPTY") @Valid List<Condition> conditions,
			@NotNull(message = "ACTIONS_NOT_NULL") @Size(min = 1, message = "ACTIONS_NOT_EMPTY") @Valid List<Action> actions) {
		super();
		this.name = name;
		this.conditions = conditions;
		this.actions = actions;
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

	public List<Action> getActions() {
		return actions;
	}

	public void setActions(List<Action> actions) {
		this.actions = actions;
	}

}
