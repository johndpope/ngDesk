package com.ngdesk.workflow.dao;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;

public class GeneratePdfNode extends Node {

	private ObjectId _id;

	@JsonProperty("PDF_TEMPLATE")
	@Field("PDF_TEMPLATE")
	private String pdfTemplate;

	public GeneratePdfNode() {

	}

	public GeneratePdfNode(String pdfTemplate) {
		super();
		this.pdfTemplate = pdfTemplate;
	}

	public String getPdfTemplate() {
		return pdfTemplate;
	}

	public void setPdfTemplate(String pdfTemplate) {
		this.pdfTemplate = pdfTemplate;
	}
}
