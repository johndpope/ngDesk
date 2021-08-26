package com.ngdesk.modules.list.layouts;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Pattern.Flag;

import com.fasterxml.jackson.annotation.JsonProperty;

public class OrderBy {

	@JsonProperty("COLUMN")
	@NotNull(message = "COLUMN_NOT_NULL")
	@Size(min = 1, message = "COLUMN_NOT_EMPTY")
	private String column;

	@JsonProperty("ORDER")
	@NotNull(message = "ORDER_NOT_NULL")
	@Size(min = 1, message = "ORDER_NOT_EMPTY")
	@Pattern(regexp = "asc|desc", flags = { Pattern.Flag.CASE_INSENSITIVE }, message = "ORDER_INVALID")
	private String order;

	public OrderBy() {

	}

	public OrderBy(@NotNull(message = "COLUMN_NOT_NULL") @Size(min = 1, message = "COLUMN_NOT_EMPTY") String column,
			@NotNull(message = "ORDER_NOT_NULL") @Size(min = 1, message = "ORDER_NOT_EMPTY") @Pattern(regexp = "asc|desc", flags = Flag.CASE_INSENSITIVE, message = "ORDER_INVALID") String order) {
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
