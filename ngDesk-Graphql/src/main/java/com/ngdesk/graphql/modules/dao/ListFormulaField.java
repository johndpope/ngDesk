package com.ngdesk.graphql.modules.dao;

import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ListFormulaField {
	
	@JsonProperty("FORMULA_NAME")
	@Field("FORMULA_NAME")
	private String formulaName;

	@JsonProperty("FORMULA_LABEL")
	@Field("FORMULA_LABEL")
	private String formulaLabel;

	@JsonProperty("FORMULA")
	@Field("FORMULA")
	private String formula;

	public ListFormulaField() {

	}

	public ListFormulaField(String formulaName, String formulaLabel, String formula) {
		super();
		this.formulaName = formulaName;
		this.formulaLabel = formulaLabel;
		this.formula = formula;
	}

	public String getFormulaName() {
		return formulaName;
	}

	public void setFormulaName(String formulaName) {
		this.formulaName = formulaName;
	}

	public String getFormulaLabel() {
		return formulaLabel;
	}

	public void setFormulaLabel(String formulaLabel) {
		this.formulaLabel = formulaLabel;
	}

	public String getFormula() {
		return formula;
	}

	public void setFormula(String formula) {
		this.formula = formula;
	}

}
