package com.ngdesk.graphql.csvimport.dao;

import java.util.Date;
import java.util.List;

import org.springframework.data.annotation.Id;

public class CsvImport {

	@Id
	private String csvImportId;

	private String status;

	private CsvImportData csvImportData;

	private String moduleId;

	private List<String> logs;

	private String companyId;

	private String name;

	private Date dateCreated;

	private String createdBy;

	public CsvImport() {

	}

	public CsvImport(String csvImportId, String status, CsvImportData csvImportData, String moduleId, List<String> logs,
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

	public List<String> getLogs() {
		return logs;
	}

	public void setLogs(List<String> logs) {
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
