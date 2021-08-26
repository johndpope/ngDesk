package com.ngdesk.channels.interval;

import java.sql.Timestamp;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ngdesk.annotations.DayOfMonthValidator;
import com.ngdesk.annotations.DayOfWeekValidator;
import com.ngdesk.annotations.HourValidator;
import com.ngdesk.annotations.MinutesValidator;
import com.ngdesk.annotations.MonthValidator;
import com.ngdesk.workflow.Workflow;

public class IntervalChannel {

	@JsonProperty("NAME")
	@NotNull(message = "CHANNEL_NAME_NOT_NULL")
	private String name;

	@JsonProperty("DESCRIPTION")
	@NotNull(message = "CHANNEL_DESCRIPTION_NOT_NULL")
	private String description;

	@JsonProperty("SOURCE_TYPE")
	@NotNull(message = "CHANNEL_SOURCE_TYPE_NOT_NULL")
	@Pattern(regexp = "interval", message = "INVALID_SOURCE_TYPE")
	private String sourceType;

	@JsonProperty("MINUTES")
	@MinutesValidator
	private String minutes;

	@JsonProperty("HOURS")
	@HourValidator
	private String hours;

	@JsonProperty("MONTH")
	@MonthValidator
	private String month;

	@JsonProperty("DAY_OF_MONTH")
	@DayOfMonthValidator
	private String dayOfMonth;

	@JsonProperty("DAY_OF_WEEK")
	@DayOfWeekValidator
	private String dayOfWeek;

	@JsonProperty("CHANNEL_ID")
	private String channelId;

	@JsonProperty("WORKFLOW")
	@Valid
	private Workflow workflow;

	@JsonProperty("DATE_CREATED")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
	private Timestamp dateCreated;

	@JsonProperty("DATE_UPDATED")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
	private Timestamp dateUpdated;

	@JsonProperty("LAST_UPDATED_BY")
	private String lastUpdated;

	public IntervalChannel() {

	}

	public IntervalChannel(@NotNull(message = "CHANNEL_NAME_NOT_NULL") String name,
			@NotNull(message = "CHANNEL_DESCRIPTION_NOT_NULL") String description,
			@NotNull(message = "CHANNEL_SOURCE_TYPE_NOT_NULL") @Pattern(regexp = "interval", message = "INVALID_SOURCE_TYPE") String sourceType,
			String minutes, String hours, String month, String dayOfMonth, String dayOfWeek, String channelId,
			@Valid Workflow workflow, Timestamp dateCreated, Timestamp dateUpdated, String lastUpdated) {
		super();
		this.name = name;
		this.description = description;
		this.sourceType = sourceType;
		this.minutes = minutes;
		this.hours = hours;
		this.month = month;
		this.dayOfMonth = dayOfMonth;
		this.dayOfWeek = dayOfWeek;
		this.channelId = channelId;
		this.workflow = workflow;
		this.dateCreated = dateCreated;
		this.dateUpdated = dateUpdated;
		this.lastUpdated = lastUpdated;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getSourceType() {
		return sourceType;
	}

	public void setSourceType(String sourceType) {
		this.sourceType = sourceType;
	}

	public String getMinutes() {
		return minutes;
	}

	public void setMinutes(String minutes) {
		this.minutes = minutes;
	}

	public String getHours() {
		return hours;
	}

	public void setHours(String hours) {
		this.hours = hours;
	}

	public String getMonth() {
		return month;
	}

	public void setMonth(String month) {
		this.month = month;
	}

	public String getDayOfMonth() {
		return dayOfMonth;
	}

	public void setDayOfMonth(String dayOfMonth) {
		this.dayOfMonth = dayOfMonth;
	}

	public String getDayOfWeek() {
		return dayOfWeek;
	}

	public void setDayOfWeek(String dayOfWeek) {
		this.dayOfWeek = dayOfWeek;
	}

	public String getChannelId() {
		return channelId;
	}

	public void setChannelId(String channelId) {
		this.channelId = channelId;
	}

	public Workflow getWorkflow() {
		return workflow;
	}

	public void setWorkflow(Workflow workflow) {
		this.workflow = workflow;
	}

	public Timestamp getDateCreated() {
		return dateCreated;
	}

	public void setDateCreated(Timestamp dateCreated) {
		this.dateCreated = dateCreated;
	}

	public Timestamp getDateUpdated() {
		return dateUpdated;
	}

	public void setDateUpdated(Timestamp dateUpdated) {
		this.dateUpdated = dateUpdated;
	}

	public String getLastUpdated() {
		return lastUpdated;
	}

	public void setLastUpdated(String lastUpdated) {
		this.lastUpdated = lastUpdated;
	}

}
