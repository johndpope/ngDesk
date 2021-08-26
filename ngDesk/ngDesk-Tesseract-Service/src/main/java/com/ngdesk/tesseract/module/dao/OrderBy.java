package com.ngdesk.tesseract.module.dao;

import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;

public class OrderBy {

	@JsonProperty("COLUMN")
	@Field("COLUMN")
	private String column;

	@JsonProperty("ORDER")
	@Field("ORDER")
	private String order;

	public OrderBy() {

	}

	public OrderBy(String column, String order) {
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
