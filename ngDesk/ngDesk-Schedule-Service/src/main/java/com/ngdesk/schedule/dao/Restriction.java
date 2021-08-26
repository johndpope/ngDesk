package com.ngdesk.schedule.dao;

import java.util.Date;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;


// @ValidRestriction
public class Restriction {

	@Field("START_TIME")
	@JsonProperty("START_TIME")
	@NotNull(message = "START_TIME_NOT_NULL")
//	@ValidTimes
	private String startTime;

	@Field("END_TIME")
	@JsonProperty("END_TIME")
	@NotNull(message = "END_TIME_CANNOT_BE_NULL")
//	@ValidTimes
	private String endTime;

	@Field("START_DAY")
	@JsonProperty("START_DAY")
	@Size(min = 1, message = "START_DAY_EMPTY")
	@Pattern(regexp = "Sun|Mon|Tue|Wed|Thu|Fri|Sat", message = "NOT_VALID_WEEK_DAY")
	private Date startDay;

	@Field("END_DAY")
	@JsonProperty("END_DAY")
	@Size(min = 1, message = "END_DAY_EMPTY")
	@Pattern(regexp = "Sun|Mon|Tue|Wed|Thu|Fri|Sat", message = "NOT_VALID_WEEK_DAY")
	private Date endDay;

	public Restriction() {

	}

	public Restriction(@NotNull(message = "START_TIME_CANNOT_BE_NULL") String startTime,
			@NotNull(message = "END_TIME_CANNOT_BE_NULL") String endTime,
			@Size(min = 1, message = "START_DAY_EMPTY") @Pattern(regexp = "Sun|Mon|Tue|Wed|Thu|Fri|Sat", message = "NOT_VALID_WEEK_DAY") Date startDay,
			@Size(min = 1, message = "END_DAY_EMPTY") @Pattern(regexp = "Sun|Mon|Tue|Wed|Thu|Fri|Sat", message = "NOT_VALID_WEEK_DAY") Date endDay) {
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

	public Date getStartDay() {
		return startDay;
	}

	public void setStartDay(Date startDay) {
		this.startDay = startDay;
	}

	public Date getEndDay() {
		return endDay;
	}

	public void setEndDay(Date endDay) {
		this.endDay = endDay;
	}



}
