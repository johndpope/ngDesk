package com.ngdesk.report.dao;

import java.util.Date;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.springframework.data.annotation.Id;

import com.ngdesk.commons.annotations.CustomNotEmpty;

public class Report {

	@Id
	private String reportId;

	@CustomNotEmpty(message = "DAO_VARIABLE_REQUIRED", values = { "REPORT_NAME" })
	@Size(min = 1, message = "REPORT_NAME_EMPTY")
	private String reportName;

	private String reportDescription;

	@CustomNotEmpty(message = "DAO_VARIABLE_REQUIRED", values = { "MODULE_NAME" })
	private String module;

	@NotNull(message = "FIELDS_NOT_NULL")
	@Valid
	private List<ReportField> fields;

	@NotNull(message = "FILTERS_NOT_NULL")
	@Valid
	private List<Filter> filters;

	private Date dateCreated;

	private Date dateUpdated;

	private String lastUpdated;

	private String createdBy;

	@Valid
	private ReportField sortBy;

	@CustomNotEmpty(message = "DAO_VARIABLE_REQUIRED", values = { "TYPE_REQUIRED" })
	@Pattern(regexp = "list", message = "NOT_VALID_TYPE")
	private String type;

	@Pattern(regexp = "asc|desc", message = "NOT_VALID_ORDER")
	private String order;

	@Valid
	private ReportSchedule schedules;

	public Report() {

	}

	public Report(String reportId, @Size(min = 1, message = "REPORT_NAME_EMPTY") String reportName,
			String reportDescription, String module, @NotNull(message = "FIELDS_NOT_NULL") @Valid List<ReportField> fields,
			@NotNull(message = "FILTERS_NOT_NULL") @Valid List<Filter> filters, Date dateCreated, Date dateUpdated,
			String lastUpdated, String createdBy, @Valid ReportField sortBy,
			@Pattern(regexp = "list", message = "NOT_VALID_TYPE") String type,
			@Pattern(regexp = "asc|desc", message = "NOT_VALID_ORDER") String order, @Valid ReportSchedule schedules) {
		super();
		this.reportId = reportId;
		this.reportName = reportName;
		this.reportDescription = reportDescription;
		this.module = module;
		this.fields = fields;
		this.filters = filters;
		this.dateCreated = dateCreated;
		this.dateUpdated = dateUpdated;
		this.lastUpdated = lastUpdated;
		this.createdBy = createdBy;
		this.sortBy = sortBy;
		this.type = type;
		this.order = order;
		this.schedules = schedules;
	}

	public String getReportName() {
		return reportName;
	}

	public void setReportName(String reportName) {
		this.reportName = reportName;
	}

	public String getReportDescription() {
		return reportDescription;
	}

	public void setReportDescription(String reportDescription) {
		this.reportDescription = reportDescription;
	}

	public String getModule() {
		return module;
	}

	public void setModule(String module) {
		this.module = module;
	}

	public List<ReportField> getFields() {
		return fields;
	}

	public void setFields(List<ReportField> fields) {
		this.fields = fields;
	}

	public List<Filter> getFilters() {
		return filters;
	}

	public void setFilters(List<Filter> filters) {
		this.filters = filters;
	}

	public Date getDateCreated() {
		return dateCreated;
	}

	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}

	public Date getDateUpdated() {
		return dateUpdated;
	}

	public void setDateUpdated(Date dateUpdated) {
		this.dateUpdated = dateUpdated;
	}

	public String getLastUpdated() {
		return lastUpdated;
	}

	public void setLastUpdated(String lastUpdated) {
		this.lastUpdated = lastUpdated;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public String getReportId() {
		return reportId;
	}

	public void setReportId(String reportId) {
		this.reportId = reportId;
	}

	public ReportField getSortBy() {
		return sortBy;
	}

	public void setSortBy(ReportField sortBy) {
		this.sortBy = sortBy;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getOrder() {
		return order;
	}

	public void setOrder(String order) {
		this.order = order;
	}

	public ReportSchedule getSchedules() {
		return schedules;
	}

	public void setSchedules(ReportSchedule schedules) {
		this.schedules = schedules;
	}

}
