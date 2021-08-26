package com.ngdesk.modules.rules;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonProperty;

public class FieldRules {

	@JsonProperty("FIELD_RULES")
	@NotNull(message = "FIELD_RULES_NOT_NULL")
	@Valid
	private List<Rule> rules;

	public FieldRules() {

	}

	public FieldRules(@NotNull(message = "FIELD_RULES_NOT_NULL") @Valid List<Rule> rules) {
		super();
		this.rules = rules;
	}

	public List<Rule> getRules() {
		return rules;
	}

	public void setRules(List<Rule> rules) {
		this.rules = rules;
	}

}
