package com.ngdesk.module.slas.dao;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

import io.swagger.v3.oas.annotations.media.Schema;

public class Recurrence {

	@Schema(description = "Max Recurrence")
	@Max(value = 20, message = "MAX_RECURRENCE_MAXIMUM_REACHED")
	private Integer maxRecurrence;

	@Schema(description = "Interval Time")
	@Min(value = 1, message = "INTERVAL_TIME_REQUIRED")
	@Max(value = 1440, message = "INTERVAL_TIME_MAXIMUM_REACHED")
	private Integer intervalTime;

	public Recurrence() {

	}

	public Recurrence(@Max(value = 20, message = "MAX_RECURRENCE_MAXIMUM_REACHED") Integer maxRecurrence,
			@Min(value = 1, message = "INTERVAL_TIME_REQUIRED") @Max(value = 1440, message = "INTERVAL_TIME_MAXIMUM_REACHED") Integer intervalTime) {
		super();
		this.maxRecurrence = maxRecurrence;
		this.intervalTime = intervalTime;
	}

	public Integer getMaxRecurrence() {
		return maxRecurrence;
	}

	public void setMaxRecurrence(Integer maxRecurrence) {
		this.maxRecurrence = maxRecurrence;
	}

	public Integer getIntervalTime() {
		return intervalTime;
	}

	public void setIntervalTime(Integer intervalTime) {
		this.intervalTime = intervalTime;
	}

}
