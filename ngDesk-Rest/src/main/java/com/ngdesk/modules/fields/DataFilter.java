package com.ngdesk.modules.fields;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ngdesk.modules.rules.Condition;

public class DataFilter {

	@JsonProperty("CONDITIONS")
	private List<Condition> conditions;

	public DataFilter(List<Condition> conditions) {
		super();
		this.conditions = conditions;
	}

	public DataFilter() {
		super();
	}

	public List<Condition> getConditions() {
		return conditions;
	}

	public void setConditions(List<Condition> conditions) {
		this.conditions = conditions;
	}

}
