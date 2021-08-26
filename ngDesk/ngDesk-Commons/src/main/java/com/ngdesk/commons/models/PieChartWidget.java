package com.ngdesk.commons.models;

import com.ngdesk.commons.annotations.CustomNotEmpty;

public class PieChartWidget extends Widget {

	@CustomNotEmpty(message = "DAO_VARIABLE_REQUIRED", values = { "FIELD" })
	private String field;

	public PieChartWidget() {

	}

	public PieChartWidget(String field) {
		super();
		this.field = field;
	}

	public String getField() {
		return field;
	}

	public void setField(String field) {
		this.field = field;
	}

}
