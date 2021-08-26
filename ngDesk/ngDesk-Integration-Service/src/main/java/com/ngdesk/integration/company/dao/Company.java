package com.ngdesk.integration.company.dao;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Company {

	@JsonProperty("COMPANY_ID")
	@Id
	private String comapnyId;

	@JsonProperty("COMPANY_SUBDOMAIN")
	@Field("COMPANY_SUBDOMAIN")
	private String companySubdomain;

	public Company(String comapnyId, String companySubdomain) {
		super();
		this.comapnyId = comapnyId;
		this.companySubdomain = companySubdomain;
	}

	public Company() {
		super();
	}

	public String getComapnyId() {
		return comapnyId;
	}

	public void setComapnyId(String comapnyId) {
		this.comapnyId = comapnyId;
	}

	public String getCompanySubdomain() {
		return companySubdomain;
	}

	public void setCompanySubdomain(String companySubdomain) {
		this.companySubdomain = companySubdomain;
	}

}
