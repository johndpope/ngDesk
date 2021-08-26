package com.ngdesk.company.rolelayout.dao;

import javax.validation.constraints.Pattern;

import com.ngdesk.commons.annotations.CustomNotEmpty;

import io.swagger.v3.oas.annotations.media.Schema;

public class OrderBy {

	@Schema(description = "Column for the order by", required = false)
	@CustomNotEmpty(message = "DAO_VARIABLE_REQUIRED", values = { "ORDER_BY_COLUMN" })
	private String column;

	@Schema(description = "Order for the order by", required = false, example = "Asc|Desc")
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
