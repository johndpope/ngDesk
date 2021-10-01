package com.ngdesk.data.dao;

import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ListFormulaFieldValue {

	@Field("FORMULA_NAME")
	@JsonProperty("FORMULA_NAME")
	private String formulaName;

	@Field("VALUE")
	@JsonProperty("VALUE")
	private Object value;

	public ListFormulaFieldValue() {

	}

	public ListFormulaFieldValue(String formulaName, Object value) {
		super();
		this.formulaName = formulaName;
		this.value = value;
	}

	public String getFormulaName() {
		return formulaName;
	}

	public void setFormulaName(String formulaName) {
		this.formulaName = formulaName;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

}
