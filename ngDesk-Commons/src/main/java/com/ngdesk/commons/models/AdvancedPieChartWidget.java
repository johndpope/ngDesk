package com.ngdesk.commons.models;

import com.ngdesk.commons.annotations.CustomNotEmpty;

public class AdvancedPieChartWidget extends Widget {
	
	@CustomNotEmpty(message = "DAO_VARIABLE_REQUIRED", values = { "FIELD" })
	private String field;
	
	
	private Object name;

	private double percentage;

	private int count;

	private long totalCount;

	public AdvancedPieChartWidget() {

	}

	public AdvancedPieChartWidget(String field, Object name, double percentage, int count, long totalCount) {
		super();
		this.name = name;
		this.percentage = percentage;
		this.count = count;
		this.totalCount = totalCount;
		this.field = field;
	}

	public Object getName() { 
		return name;
	}

	public void setName(Object name) {
		this.name = name;
	}

	public double getPercentage() {
		return percentage;
	}

	public void setPercentage(double percentage) {
		this.percentage = percentage;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public long getTotalCount() {
		return totalCount;
	}

	public void setTotalCount(long totalCount) {
		this.totalCount = totalCount;
	}
	
	public String getField() {
		return field;
	}

	public void setField(String field) {
		this.field = field;
	}

}
