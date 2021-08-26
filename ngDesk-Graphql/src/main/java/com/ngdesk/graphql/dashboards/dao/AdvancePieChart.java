package com.ngdesk.graphql.dashboards.dao;

import com.ngdesk.commons.models.Widget;

public class AdvancePieChart extends Widget {
	private Object name;

	private Double percentage;

	private Double count;

	private long totalCount;

	public AdvancePieChart() {

	}

	public AdvancePieChart(Object name, Double percentage, Double count, long totalCount) {
		super();
		this.name = name;
		this.percentage = percentage;
		this.count = count;
		this.totalCount = totalCount;
	}

	public Object getName() {
		return name;
	}

	public void setName(Object name) {
		this.name = name;
	}

	public Double getPercentage() {
		return percentage;
	}

	public void setPercentage(Double percentage) {
		this.percentage = percentage;
	}

	public Double getCount() {
		return count;
	}

	public void setCount(Double count) {
		this.count = count;
	}

	public long getTotalCount() {
		return totalCount;
	}

	public void setTotalCount(long totalCount) {
		this.totalCount = totalCount;
	}

}
