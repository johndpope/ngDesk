package com.ngdesk.company.settings.dao;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ngdesk.commons.annotations.CustomNotNull;

import io.swagger.v3.oas.annotations.media.Schema;

public class ChatRestrictions {

	@Schema(description = "Start time", required = true, example = "00:00")
	@CustomNotNull(message = "START_TIME_CANNOT_BE_NULL", values = { "START_TIME" })
	@Field("START_TIME")
	@JsonProperty("START_TIME")
	private String startTime;

	@Schema(description = "End time", required = true, example = "00:00")
	@CustomNotNull(message = "END_TIME_CANNOT_BE_NULL", values = { "END_TIME" })
	@Field("END_TIME")
	@JsonProperty("END_TIME")
	private String endTime;

	@Schema(description = "Start day", required = false, example = "Mon")
	@Size(min = 1, message = "START_DAY_EMPTY")
	@Pattern(regexp = "Sun|Mon|Tue|Wed|Thu|Fri|Sat", message = "NOT_VALID_WEEK_DAY")
	@Field("START_DAY")
	@JsonProperty("START_DAY")
	private String startDay;

	@Schema(description = "End day", required = false, example = "Sat")
	@Size(min = 1, message = "END_DAY_EMPTY")
	@Pattern(regexp = "Sun|Mon|Tue|Wed|Thu|Fri|Sat", message = "NOT_VALID_WEEK_DAY")
	@Field("END_DAY")
	@JsonProperty("END_DAY")
	private String endDay;

	public ChatRestrictions() {

	}

	public ChatRestrictions(String startTime, String endTime,
			@Size(min = 1, message = "START_DAY_EMPTY") @Pattern(regexp = "Sun|Mon|Tue|Wed|Thu|Fri|Sat", message = "NOT_VALID_WEEK_DAY") String startDay,
			@Size(min = 1, message = "END_DAY_EMPTY") @Pattern(regexp = "Sun|Mon|Tue|Wed|Thu|Fri|Sat", message = "NOT_VALID_WEEK_DAY") String endDay) {
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
