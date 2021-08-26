package com.ngdesk.graphql.reports.dao;

public class Filter {

	private ReportField field;

	private String operator;

	private String requirementType;

	private String value;

	public Filter() {

	}

	public Filter(ReportField field, String operator, String requirementType, String value) {
		super();
		this.field = field;
		this.operator = operator;
		this.requirementType = requirementType;
		this.value = value;
	}

	public ReportField getField() {
		return field;
	}

	public void setField(ReportField field) {
		this.field = field;
	}

	public String getOperator() {
		return operator;
	}

	public void setOperator(String operator) {
		this.operator = operator;
	}

	public String getRequirementType() {
		return requirementType;
	}

	public void setRequirementType(String requirementType) {
		this.requirementType = requirementType;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

}
