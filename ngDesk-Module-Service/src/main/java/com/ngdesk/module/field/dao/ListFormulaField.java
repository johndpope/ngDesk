package com.ngdesk.module.field.dao;

import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ngdesk.commons.annotations.CustomNotEmpty;
import com.ngdesk.commons.annotations.CustomNotNull;

import io.swagger.v3.oas.annotations.media.Schema;

public class ListFormulaField {

	@Schema(description = "Name of the formula")
	@Field("FORMULA_NAME")
	@JsonProperty("FORMULA_NAME")
	private String formulaName;

	@Schema(description = "Label of the formula")
	@Field("FORMULA_LABEL")
	@JsonProperty("FORMULA_LABEL")
	private String formulaLabel;

	@Schema(description = "Formula to calculate value")
	@Field("FORMULA")
	@JsonProperty("FORMULA")
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
