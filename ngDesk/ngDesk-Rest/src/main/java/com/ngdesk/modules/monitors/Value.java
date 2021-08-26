package com.ngdesk.modules.monitors;

import java.util.List;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ngdesk.annotations.ValidValue;

public class Value {

	@JsonProperty("VALUE")
	@NotNull(message = "VALUE_NOT_NULL")
	@Size(min = 1, message = "VALUE_EMPTY")
	private String value;

	@JsonProperty("ORDER")
	@NotNull(message = "ORDER_NOT_NULL")
	private int order;

	public Value() {

	}

	public Value(@NotNull(message = "VALUE_NOT_NULL") @Size(min = 1, message = "VALUE_EMPTY") String value,
			@NotNull(message = "ORDER_NOT_NULL") int order) {
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

	public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		this.order = order;
	}

}
