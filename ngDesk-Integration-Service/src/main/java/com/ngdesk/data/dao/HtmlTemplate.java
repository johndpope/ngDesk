package com.ngdesk.data.dao;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;

public class HtmlTemplate {

	@JsonProperty("MODULE")
	@Field("MODULE")
	private String moduleId;

	@Field("HTML_TEMPLATE")
	@JsonProperty("HTML_TEMPLATE")
	private String htmlTemplate;

	@Field("TITLE")
	@JsonProperty("TITLE")
	private String name;

	@JsonProperty("TEMPLATE_ID")
	@Id
	private String templateId;

	public HtmlTemplate() {
	}

	public HtmlTemplate(String moduleId, String htmlTemplate, String name, String templateId) {
		super();
		this.moduleId = moduleId;
		this.htmlTemplate = htmlTemplate;
		this.name = name;
		this.templateId = templateId;
	}

	public String getModuleId() {
		return moduleId;
	}

	public void setModuleId(String moduleId) {
		this.moduleId = moduleId;
	}

	public String getHtmlTemplate() {
		return htmlTemplate;
	}

	public void setHtmlTemplate(String htmlTemplate) {
		this.htmlTemplate = htmlTemplate;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getTemplateId() {
		return templateId;
	}

	public void setTemplateId(String templateId) {
		this.templateId = templateId;
	}

}
