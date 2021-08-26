package com.ngdesk.channels.chat.triggers;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Value {

	@JsonProperty("VALUE")
	@NotEmpty(message = "VALUE_REQUIRED")
	private String value;

	@JsonProperty("ORDER")
	@NotNull(message = "ORDER_REQUIRED")
	private Integer order;

	public Value() {

	}

	public Value(@NotEmpty(message = "VALUE_REQUIRED") String value,
			@NotEmpty(message = "ORDER_REQUIRED") Integer order) {
		super();
		this.value = value;
		this.order = order;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public Integer getOrder() {
		return order;
	}

	public void setOrder(Integer order) {
		this.order = order;
	}

}
