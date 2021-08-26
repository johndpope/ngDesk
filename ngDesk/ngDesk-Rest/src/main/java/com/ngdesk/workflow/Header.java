package com.ngdesk.workflow;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Header {
	@JsonProperty("KEY")
	private String key;

	@JsonProperty("VALUE")
	private String value;

	public Header() {

	}

	public Header(String key, String value) {
		super();
		this.key = key;
		this.value = value;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

}
