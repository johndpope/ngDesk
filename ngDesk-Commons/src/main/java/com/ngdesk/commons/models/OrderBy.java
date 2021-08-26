package com.ngdesk.commons.models;

import javax.validation.constraints.Pattern;

import com.ngdesk.commons.annotations.CustomNotEmpty;

public class OrderBy {

	@CustomNotEmpty(message = "DAO_VARIABLE_REQUIRED", values = { "ORDER_BY_COLUMN" })
	private String column;

	@CustomNotEmpty(message = "DAO_VARIABLE_REQUIRED", values = { "ORDER_BY_ORDER" })
	@Pattern(regexp = "Asc|Desc", message = "ORDER_BY_INVALID")
	private String order;

	public OrderBy() {

	}

	public OrderBy(String column, @Pattern(regexp = "Asc|Desc", message = "ORDER_BY_INVALID") String order) {
		super();
		this.column = column;
		this.order = order;
	}

	public String getColumn() {
		return column;
	}

	public void setColumn(String column) {
		this.column = column;
	}

	public String getOrder() {
		return order;
	}

	public void setOrder(String order) {
		this.order = order;
	}
}
