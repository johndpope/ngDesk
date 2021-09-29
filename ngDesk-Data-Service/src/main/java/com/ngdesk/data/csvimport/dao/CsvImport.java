package com.ngdesk.data.csvimport.dao;

import java.util.Date;
import java.util.List;

import org.springframework.data.annotation.Id;

public class CsvImport {

	@Id
	private String csvImportId;

	private String status;

	private CsvImportData csvImportData;

	private String moduleId;

	private List<CsvImportLog> logs;

	private String companyId;

	private String name;

	private CsvFormat csvFormat;

	private Date dateCreated;

	private String createdBy;

	public CsvImport() {

	}

	public CsvImport(String csvImportId, String status, CsvImportData csvImportData, String moduleId,
			List<CsvImportLog> logs, String companyId, String name, CsvFormat csvFormat, Date dateCreated,
			String createdBy) {
		super();
		this.csvImportId = csvImportId;
		this.status = status;
		this.csvImportData = csvImportData;
		this.moduleId = moduleId;
		this.logs = logs;
		this.companyId = companyId;
		this.name = name;
		this.csvFormat = csvFormat;
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

	public List<CsvImportLog> getLogs() {
		return logs;
	}

	public void setLogs(List<CsvImportLog> logs) {
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

	public CsvFormat getCsvFormat() {
		return csvFormat;
	}

	public void setCsvFormat(CsvFormat csvFormat) {
		this.csvFormat = csvFormat;
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
