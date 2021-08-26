package com.ngdesk.graphql.reports.dao;

import java.util.List;

public class ReportSchedule {

	private String cron;

	private List<String> emails;

	public ReportSchedule() {
	}

	public ReportSchedule(String cron, List<String> emails) {
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
