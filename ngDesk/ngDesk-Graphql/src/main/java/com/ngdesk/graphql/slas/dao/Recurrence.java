package com.ngdesk.graphql.slas.dao;

public class Recurrence {

	private int maxRecurrence;

	private int intervalTime;

	public Recurrence() {

	}

	public Recurrence( int maxRecurrence,
			 int intervalTime) {
		super();
		this.maxRecurrence = maxRecurrence;
		this.intervalTime = intervalTime;
	}

	public int getMaxRecurrence() {
		return maxRecurrence;
	}

	public void setMaxRecurrence(int maxRecurrence) {
		this.maxRecurrence = maxRecurrence;
	}

	public int getIntervalTime() {
		return intervalTime;
	}

	public void setIntervalTime(int intervalTime) {
		this.intervalTime = intervalTime;
	}

}
