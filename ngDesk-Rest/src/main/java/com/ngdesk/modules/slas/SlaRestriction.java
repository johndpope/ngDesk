package com.ngdesk.modules.slas;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ngdesk.annotations.ValidRestriction;
import com.ngdesk.annotations.ValidTimes;

@ValidRestriction
public class SlaRestriction {

	@NotNull(message = "START_TIME_CANNOT_BE_NULL")
	@ValidTimes
	@JsonProperty("START_TIME")
	private String startTime;

	@NotNull(message = "END_TIME_CANNOT_BE_NULL")
	@ValidTimes
	@JsonProperty("END_TIME")
	private String endTime;

	@JsonProperty("START_DAY")
	@Size(min = 1, message = "START_DAY_EMPTY")
	@Pattern(regexp = "Sun|Mon|Tue|Wed|Thu|Fri|Sat", message = "NOT_VALID_WEEK_DAY")
	private String startDay;

	@JsonProperty("END_DAY")
	@Size(min = 1, message = "END_DAY_EMPTY")
	@Pattern(regexp = "Sun|Mon|Tue|Wed|Thu|Fri|Sat", message = "NOT_VALID_WEEK_DAY")
	private String endDay;

	public SlaRestriction() {

	}

	public SlaRestriction(@NotNull(message = "START_TIME_CANNOT_BE_NULL") String startTime,
			@NotNull(message = "END_TIME_CANNOT_BE_NULL") String endTime,
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
