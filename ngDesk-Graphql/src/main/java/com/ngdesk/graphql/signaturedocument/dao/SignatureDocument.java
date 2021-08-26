package com.ngdesk.graphql.signaturedocument.dao;

import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Field;

public class SignatureDocument {

	@Id
	private String templateId;

	private String companyId;

	private String htmlDocument;

	private String dataId;

	private String moduleId;

	private String name;

	private Boolean signed;

	private Date dateCreated;

	private Date dateSigned;

	private String emailAddress;

	public SignatureDocument() {

	}

	public SignatureDocument(String templateId, String companyId, String htmlDocument, String dataId, String moduleId,
			String name, Boolean signed, Date dateCreated, Date dateSigned, String emailAddress) {
		super();
		this.templateId = templateId;
		this.companyId = companyId;
		this.htmlDocument = htmlDocument;
		this.dataId = dataId;
		this.moduleId = moduleId;
		this.name = name;
		this.signed = signed;
		this.dateCreated = dateCreated;
		this.dateSigned = dateSigned;
		this.emailAddress = emailAddress;
	}

	public String getTemplateId() {
		return templateId;
	}

	public void setTemplateId(String itemplateIdd) {
		this.templateId = templateId;
	}

	public String getCompanyId() {
		return companyId;
	}

	public void setCompanyId(String companyId) {
		this.companyId = companyId;
	}

	public String getHtmlDocument() {
		return htmlDocument;
	}

	public void setHtmlDocument(String htmlDocument) {
		this.htmlDocument = htmlDocument;
	}

	public String getDataId() {
		return dataId;
	}

	public void setDataId(String dataId) {
		this.dataId = dataId;
	}

	public String getModuleId() {
		return moduleId;
	}

	public void setModuleId(String moduleId) {
		this.moduleId = moduleId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Boolean getSigned() {
		return signed;
	}

	public void setSigned(Boolean signed) {
		this.signed = signed;
	}

	public Date getDateCreated() {
		return dateCreated;
	}

	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}

	public Date getDateSigned() {
		return dateSigned;
	}

	public void setDateSigned(Date dateSigned) {
		this.dateSigned = dateSigned;
	}

	public String getEmailAddress() {
		return emailAddress;
	}

	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}

}
