package com.ngdesk.sam.normalizationrules.dao;

import java.util.Date;

import javax.validation.Valid;

import org.springframework.data.annotation.Id;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.ngdesk.commons.annotations.CustomNotEmpty;

public class NormalizationRule {

	@Id
	private String normalizationRuleId;

	@CustomNotEmpty(message = "DAO_VARIABLE_REQUIRED", values = { "NAME" })
	private String name;

	private String description;

	@JsonIgnore
	private String companyId;

	@Valid
	private Rule publisher;

	@Valid
	private Rule product;

	@Valid
	private Rule version;

	private String status;

	private Date dateCreated;

	public NormalizationRule() {

	}

	public NormalizationRule(String normalizationRuleId, String name, String description, String companyId,
			@Valid Rule publisher, @Valid Rule product, @Valid Rule version, String status, Date dateCreated) {
		super();
		this.normalizationRuleId = normalizationRuleId;
		this.name = name;
		this.description = description;
		this.companyId = companyId;
		this.publisher = publisher;
		this.product = product;
		this.version = version;
		this.status = status;
		this.dateCreated = dateCreated;
	}

	public String getNormalizationRuleId() {
		return normalizationRuleId;
	}

	public void setNormalizationRuleId(String normalizationRuleId) {
		this.normalizationRuleId = normalizationRuleId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getCompanyId() {
		return companyId;
	}

	public void setCompanyId(String companyId) {
		this.companyId = companyId;
	}

	public Rule getPublisher() {
		return publisher;
	}

	public void setPublisher(Rule publisher) {
		this.publisher = publisher;
	}

	public Rule getProduct() {
		return product;
	}

	public void setProduct(Rule product) {
		this.product = product;
	}

	public Rule getVersion() {
		return version;
	}

	public void setVersion(Rule version) {
		this.version = version;
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

}
