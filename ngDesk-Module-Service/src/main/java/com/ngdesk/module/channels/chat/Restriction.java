package com.ngdesk.module.channels.chat;

import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Restriction {

	@JsonProperty("START_TIME")
	@Field("START_TIME")
	private String startTime;

	@JsonProperty("END_TIME")
	@Field("END_TIME")
	private String endTime;

	@JsonProperty("START_DAY")
	@Field("START_DAY")
	private String startDay;

	@JsonProperty("END_DAY")
	@Field("END_DAY")
	private String endDay;

	public Restriction() {

	}

	public Restriction(String startTime, String endTime, String startDay, String endDay) {
		this.startTime = startTime;
		this.endTime = endTime;
		this.startDay = startDay;
		this.endDay = endDay;
	}

	public String getStartTime() {
		return startTime;
	}

	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}

	public String getEndTime() {
		return endTime;
	}

	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}

	public String getStartDay() {
		return startDay;
	}

	public void setStartDay(String startDay) {
		this.startDay = startDay;
	}

	public String getEndDay() {
		return endDay;
	}

	public void setEndDay(String endDay) {
		this.endDay = endDay;
	}

}
