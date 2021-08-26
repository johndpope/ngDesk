package com.ngdesk.reporting;

import java.sql.Timestamp;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Report {

	@JsonProperty("NAME")
	@NotNull(message = "REPORT_NAME_NOT_NULL")
	@Size(min = 1, message = "REPORT_NAME_EMPTY")
	private String reportName;

	@JsonProperty("DESCRIPTION")
	private String reportDescription;

	@JsonProperty("MODULE")
	@NotNull(message = "MODULE_NAME_NOT_NULL")
	@Size(min = 1, message = "MODULE_NAME_EMPTY")
	private String module;

	@JsonProperty("FIELDS")
	@NotNull(message = "FIELDS_NOT_NULL")
	@Size(min = 1, message = "FIELDS_EMPTY")
	@Valid
	private List<Field> fields;

	@JsonProperty("FILTERS")
	@NotNull(message = "FILTERS_NOT_NULL")
	@Valid
	private List<Filter> filters;

	@JsonProperty("DATE_CREATED")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
	private Timestamp dateCreated;

	@JsonProperty("DATE_UPDATED")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
	private Timestamp dateUpdated;

	@JsonProperty("LAST_UPDATED_BY")
	private String lastUpdated;

	@JsonProperty("CREATED_BY")
	private String createdBy;

	@JsonProperty("REPORT_ID")
	private String reportId;

	@JsonProperty("SORT_BY")
	@Valid
	private Field sortBy;

	@JsonProperty("TYPE")
	@NotEmpty(message = "TYPE_REQUIRED")
	@Pattern(regexp = "list", message = "NOT_VALID_TYPE")
	private String type;

	@JsonProperty("ORDER")
	@Pattern(regexp = "asc|desc", message = "NOT_VALID_ORDER")
	private String order;

	@JsonProperty("SCHEDULES")
	@Valid
	private ReportSchedule schedules;

	public Report() {

	}

	public Report(
			@NotNull(message = "REPORT_NAME_NOT_NULL") @Size(min = 1, message = "REPORT_NAME_EMPTY") String reportName,
			String reportDescription,
			@NotNull(message = "MODULE_NAME_NOT_NULL") @Size(min = 1, message = "MODULE_NAME_EMPTY") String module,
			@NotNull(message = "FIELDS_NOT_NULL") @Size(min = 1, message = "FIELDS_EMPTY") @Valid List<Field> fields,
			@NotNull(message = "FILTERS_NOT_NULL") @Valid List<Filter> filters, Timestamp dateCreated,
			Timestamp dateUpdated, String lastUpdated, String createdBy, String reportId, @Valid Field sortBy,
			@NotEmpty(message = "TYPE_REQUIRED") @Pattern(regexp = "list", message = "NOT_VALID_TYPE") String type,
			@Pattern(regexp = "asc|desc", message = "NOT_VALID_ORDER") String order, ReportSchedule schedules) {
		super();
		this.reportName = reportName;
		this.reportDescription = reportDescription;
		this.module = module;
		this.fields = fields;
		this.filters = filters;
		this.dateCreated = dateCreated;
		this.dateUpdated = dateUpdated;
		this.lastUpdated = lastUpdated;
		this.createdBy = createdBy;
		this.reportId = reportId;
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

	public List<Field> getFields() {
		return fields;
	}

	public void setFields(List<Field> fields) {
		this.fields = fields;
	}

	public List<Filter> getFilters() {
		return filters;
	}

	public void setFilters(List<Filter> filters) {
		this.filters = filters;
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

	public Field getSortBy() {
		return sortBy;
	}

	public void setSortBy(Field sortBy) {
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
