package com.ngdesk.module.task.dao;

import javax.validation.constraints.Pattern;

public class Interval {

	@Pattern(regexp = "Hour|Day|Month|Year")
	private String intervalType;

	
	private int intervalValue;

	public Interval() {
		super();
	}

	public Interval(@Pattern(regexp = "Hour|Day|Month|Year") String intervalType,
			 int intervalValue) {
		super();
		this.intervalType = intervalType;
		this.intervalValue = intervalValue;
	}

	public String getIntervalType() {
		return intervalType;
	}

	public void setIntervalType(String intervalType) {
		this.intervalType = intervalType;
	}

	public int getIntervalValue() {
		return intervalValue;
	}

	public void setIntervalValue(int intervalValue) {
		this.intervalValue = intervalValue;
	}

	

}
