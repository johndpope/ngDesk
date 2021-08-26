package com.ngdesk.module.field.dao;

import java.util.List;

import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ngdesk.module.layout.dao.Condition;


public class DataFilter {

	@Field("CONDITIONS")
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
