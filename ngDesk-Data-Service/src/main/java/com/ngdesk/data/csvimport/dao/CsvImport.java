package com.ngdesk.data.csvimport.dao;

import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CsvImport {

	@Id
	private String csvImportId;

	@Field("STATUS")
	@JsonProperty("STATUS")
	private String status;

	@Field("CSV_IMPORT_DATA")
	@JsonProperty("CSV_IMPORT_DATA")
	private CsvImportData csvImportData;

	@Field("MODULE_ID")
	@JsonProperty("MODULE_ID")
	private String moduleId;

	@Field("LOGS")
	@JsonProperty("LOGS")
	private String logs;

	@Field("COMPANY_ID")
	@JsonProperty("COMPANY_ID")
	private String companyId;

	@Field("NAME")
	@JsonProperty("NAME")
	private String name;

	@Field("DATE_CREATED")
	@JsonProperty("DATE_CREATED")
	private Date dateCreated;

	@Field("CREATED_BY")
	@JsonProperty("CREATED_BY")
	private String createdBy;

	public CsvImport() {
	}

	public CsvImport(String csvImportId, String status, CsvImportData csvImportData, String moduleId, String logs,
			String companyId, String name, Date dateCreated, String createdBy) {
		super();
		this.csvImportId = csvImportId;
		this.status = status;
		this.csvImportData = csvImportData;
		this.moduleId = moduleId;
		this.logs = logs;
		this.companyId = companyId;
		this.name = name;
		this.dateCreated = dateCreated;
		this.createdBy = createdBy;
	}

	public String getCsvImportId() {
		return csvImportId;
	}

	public void setCsvImportId(String csvImportId) {
		this.csvImportId = csvImportId;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public CsvImportData getCsvImportData() {
		return csvImportData;
	}

	public void setCsvImportData(CsvImportData csvImportData) {
		this.csvImportData = csvImportData;
	}

	public String getModuleId() {
		return moduleId;
	}

	public void setModuleId(String moduleId) {
		this.moduleId = moduleId;
	}

	public String getLogs() {
		return logs;
	}

	public void setLogs(String logs) {
		this.logs = logs;
	}

	public String getCompanyId() {
		return companyId;
	}

	public void setCompanyId(String companyId) {
		this.companyId = companyId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Date getDateCreated() {
		return dateCreated;
	}

	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

}
