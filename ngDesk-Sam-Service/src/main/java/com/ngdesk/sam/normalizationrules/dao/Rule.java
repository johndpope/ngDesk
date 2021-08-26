package com.ngdesk.sam.normalizationrules.dao;

import javax.validation.constraints.Pattern;

import com.ngdesk.commons.annotations.CustomNotEmpty;

public class Rule {

	@CustomNotEmpty(message = "DAO_VARIABLE_REQUIRED", values = { "key" })
	private String key;

	@Pattern(regexp = "Is|Starts With|Ends With|Contains", message = "Invalid operator")
	@CustomNotEmpty(message = "DAO_VARIABLE_REQUIRED", values = { "operator" })
	private String operator;

	@CustomNotEmpty(message = "DAO_VARIABLE_REQUIRED", values = { "value" })
	private String value;

	public Rule() {

	}

	public Rule(String key,
			@Pattern(regexp = "Is|Starts With|Ends With|Contains", message = "Invalid operator") String operator,
			String value) {
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
