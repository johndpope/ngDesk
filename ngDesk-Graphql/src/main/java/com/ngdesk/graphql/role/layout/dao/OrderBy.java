package com.ngdesk.graphql.role.layout.dao;

public class OrderBy {

	private String column;

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
