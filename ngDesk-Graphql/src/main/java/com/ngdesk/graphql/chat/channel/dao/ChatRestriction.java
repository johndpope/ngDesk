package com.ngdesk.graphql.chat.channel.dao;

import org.springframework.data.mongodb.core.mapping.Field;

public class ChatRestriction {

	@Field("START_TIME")
	private String startTime;

	@Field("END_TIME")
	private String endTime;

	@Field("START_DAY")
	private String startDay;

	@Field("END_DAY")
	private String endDay;

	public ChatRestriction() {

	}

	public ChatRestriction(String startTime, String endTime, String startDay, String endDay) {
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
