package com.ngdesk.reporting;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ngdesk.annotations.CronValid;

public class ReportSchedule {

	@JsonProperty("CRON")
	@NotNull(message = "CRON_NOT_NULL")
	@CronValid
	private String cron;

	@JsonProperty("EMAILS")
	@Size(min = 1, message = "SIZE_LESS_THAN_ONE")
	@Size(max = 10, message = "SIZE_GREATER_THAN_TEN")
	private List<String> emails;

	public ReportSchedule() {
	}

	public ReportSchedule(@NotNull(message = "CRON_NOT_NULL") String cron,
			@Size(min = 1, max = 10) List<String> emails) {
		super();
		this.cron = cron;
		this.emails = emails;
	}

	public String getCron() {
		return cron;
	}

	public void setCron(String cron) {
		this.cron = cron;
	}

	public List<String> getEmails() {
		return emails;
	}

	public void setEmails(List<String> emails) {
		this.emails = emails;
	}

}
