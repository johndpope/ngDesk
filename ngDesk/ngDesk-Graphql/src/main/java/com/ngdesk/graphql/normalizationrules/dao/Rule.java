package com.ngdesk.graphql.normalizationrules.dao;

public class Rule {

	private String key;

	private String operator;

	private String value;

	public Rule() {

	}

	public Rule(String key, String operator, String value) {
		super();
		this.key = key;
		this.operator = operator;
		this.value = value;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getOperator() {
		return operator;
	}

	public void setOperator(String operator) {
		this.operator = operator;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
}
