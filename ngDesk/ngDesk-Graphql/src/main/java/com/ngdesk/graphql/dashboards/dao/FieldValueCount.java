package com.ngdesk.graphql.dashboards.dao;

public class FieldValueCount {
	private Object name;
	private Double value;
	private Object id;

	FieldValueCount() {

	}

	public FieldValueCount(Object name, Double value, Object id) {
		super();
		this.name = name;
		this.value = value;
		this.id = id;
	}

	public Object getId() {
		return id;
	}

	public void setId(Object id) {
		this.id = id;
	}

	public Object getName() {
		return name;
	}

	public void setName(Object name) {
		this.name = name;
	}

	public Double getValue() {
		return value;
	}

	public void setValue(Double value) {
		this.value = value;
	}

}
