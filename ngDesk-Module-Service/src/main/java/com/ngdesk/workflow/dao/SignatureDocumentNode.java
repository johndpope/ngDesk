package com.ngdesk.workflow.dao;

import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ngdesk.commons.annotations.CustomNotEmpty;

import io.swagger.v3.oas.annotations.media.Schema;

public class SignatureDocumentNode extends Node {

	@Schema(required = true, description = "title of the Generate pdf node", example = "Generate PDF Node")
	@JsonProperty("PDF_TEMPLATE_ID")
	@Field("PDF_TEMPLATE_ID")
	@CustomNotEmpty(message = "DAO_VARIABLE_REQUIRED", values = { "PDF_TEMPLATE_ID" })
	private String pdfTemplateId;

	@Schema(required = true, description = "email address of the person to whom the mail is to be sent", example = "ngdesk-devs@ngdesk.com")
	@JsonProperty("TO")
	@Field("TO")
	@CustomNotEmpty(message = "DAO_VARIABLE_REQUIRED", values = { "TO" })
	private String to;

	@Schema(required = true, description = "email address of the person to whom the mail is to be sent", example = "support@subdomain.ngdesk.com")
	@JsonProperty("FROM")
	@Field("FROM")
	@CustomNotEmpty(message = "DAO_VARIABLE_REQUIRED", values = { "FROM" })
	private String from;

	@Schema(required = true, description = "subject of the email", example = "Testing email node")
	@JsonProperty("SUBJECT")
	@Field("SUBJECT")
	@CustomNotEmpty(message = "DAO_VARIABLE_REQUIRED", values = { "SUBJECT" })
	private String subject;

	@JsonProperty("FIELD_ID")
	@Field("FIELD_ID")
	@Schema(required = true, description = "Field Id")
	@CustomNotEmpty(message = "DAO_VARIABLE_REQUIRED", values = { "FIELD_ID" })
	private String fieldId;

	public SignatureDocumentNode() {

	}

	public SignatureDocumentNode(String pdfTemplateId, String to, String from, String subject, String fieldId) {
		super();
		this.pdfTemplateId = pdfTemplateId;
		this.to = to;
		this.from = from;
		this.subject = subject;
		this.fieldId = fieldId;
	}

	public String getPdfTemplateId() {
		return pdfTemplateId;
	}

	public void setPdfTemplateId(String pdfTemplateId) {
		this.pdfTemplateId = pdfTemplateId;
	}

	public String getTo() {
		return to;
	}

	public void setTo(String to) {
		this.to = to;
	}

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getFieldId() {
		return fieldId;
	}

	public void setFieldId(String fieldId) {
		this.fieldId = fieldId;
	}

}
