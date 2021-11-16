package com.ngdesk.websocket.companies.dao;

import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ChatRestrictions {

	@Field("START_TIME")
	@JsonProperty("START_TIME")
	private String startTime;

	@Field("END_TIME")
	@JsonProperty("END_TIME")
	private String endTime;

	@Field("START_DAY")
	@JsonProperty("START_DAY")
	private String startDay;

	@Field("END_DAY")
	@JsonProperty("END_DAY")
	private String endDay;

	public ChatRestrictions() {

	}

	public ChatRestrictions(String startTime, String endTime, String startDay, String endDay) {
		super();
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
