package com.ngdesk.workflow.dao;

import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;

public class JavascriptNode extends Node {
	@JsonProperty("CODE")
	@Field("CODE")
	private String code;

	public JavascriptNode() {
	}

	public JavascriptNode(String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

}
