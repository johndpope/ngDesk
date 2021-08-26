package com.ngdesk.tesseract.module.dao;

import java.util.List;

import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DataFilter {

	@JsonProperty("CONDITIONS")
	@Field("CONDITIONS")
	private List<Condition> conditions;

	public DataFilter() {
	}

	public DataFilter(List<Condition> conditions) {
		super();
		this.conditions = conditions;
	}

	public List<Condition> getConditions() {
		return conditions;
	}

	public void setConditions(List<Condition> conditions) {
		this.conditions = conditions;
	}

}
