package com.ngdesk.module.slas.dao;

import io.swagger.v3.oas.annotations.media.Schema;

public class SLARestriction {

	@Schema(description = "Start Day")
	private String startDay;

	@Schema(description = "Start time")
	private String startTime;

	@Schema(description = "End Day")
	private String endDay;

	@Schema(description = "End Time")
	private String endTime;

	

	public SLARestriction() {
		
	}

	public SLARestriction(String startDay, String startTime, String endDay, String endTime) {
		super();
		this.startDay = startDay;
		this.startTime = startTime;
		this.endDay = endDay;
		this.endTime = endTime;
	}

	public String getStartDay() {
		return startDay;
	}

	public void setStartDay(String startDay) {
		this.startDay = startDay;
	}

	public String getStartTime() {
		return startTime;
	}

	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}

	public String getEndDay() {
		return endDay;
	}

	public void setEndDay(String endDay) {
		this.endDay = endDay;
	}

	public String getEndTime() {
		return endTime;
	}

	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}

}
