package com.ngdesk.graphql.userplugin.dao;

import java.util.Date;
import java.util.List;

import org.springframework.data.annotation.Id;

public class UserPlugin {

	@Id
	private String id;

	private String name;

	private List<String> modules;

	private String status;

	private Date dateCreated;

	private Date dateUpdated;

	private String createdBy;

	private String lastUpdatedBy;

	private String companyId;

	public UserPlugin() {

	}

	public UserPlugin(String id, String name, List<String> modules, String status, Date dateCreated, Date dateUpdated,
			String createdBy, String lastUpdatedBy, String companyId) {
		super();
		this.id = id;
		this.name = name;
		this.modules = modules;
		this.status = status;
		this.dateCreated = dateCreated;
		this.dateUpdated = dateUpdated;
		this.createdBy = createdBy;
		this.lastUpdatedBy = lastUpdatedBy;
		this.companyId = companyId;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<String> getModules() {
		return modules;
	}

	public void setModules(List<String> modules) {
		this.modules = modules;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
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

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public String getLastUpdatedBy() {
		return lastUpdatedBy;
	}

	public void setLastUpdatedBy(String lastUpdatedBy) {
		this.lastUpdatedBy = lastUpdatedBy;
	}

	public String getCompanyId() {
		return companyId;
	}

	public void setCompanyId(String companyId) {
		this.companyId = companyId;
	}

}
