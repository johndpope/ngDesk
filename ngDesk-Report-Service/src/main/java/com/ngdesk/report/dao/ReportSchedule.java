package com.ngdesk.report.dao;

import java.util.List;

import javax.validation.constraints.Size;

import com.ngdesk.commons.annotations.CustomNotEmpty;

public class ReportSchedule {

	@CustomNotEmpty(message = "DAO_VARIABLE_REQUIRED", values = { "CRON_NOT_NULL" })
	private String cron;

	@Size(min = 1, message = "SIZE_LESS_THAN_ONE")
	@Size(max = 10, message = "SIZE_GREATER_THAN_TEN")
	private List<String> emails;

	public ReportSchedule() {
	}

	public ReportSchedule(String cron,
			@Size(min = 1, message = "SIZE_LESS_THAN_ONE") @Size(max = 10, message = "SIZE_GREATER_THAN_TEN") List<String> emails) {
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
