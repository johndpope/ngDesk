package com.ngdesk.modules.layouts;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PreDefinedTemplate {

	@JsonProperty("HTML_TEMPLATE")
	private String htmlTemplate;

	@JsonProperty("MODULE_NAME")
	private String moduleName;

	@JsonProperty("LAYOUT_TYPE")
	private String layoutType;

	public PreDefinedTemplate() {

	}

	public PreDefinedTemplate(String htmlTemplate, String moduleName, String layoutType) {
		super();
		this.htmlTemplate = htmlTemplate;
		this.moduleName = moduleName;
		this.layoutType = layoutType;
	}

	public String getHtmlTemplate() {
		return htmlTemplate;
	}

	public void setHtmlTemplate(String htmlTemplate) {
		this.htmlTemplate = htmlTemplate;
	}

	public String getModuleName() {
		return moduleName;
	}

	public void setModuleName(String moduleName) {
		this.moduleName = moduleName;
	}

	public String getLayoutType() {
		return layoutType;
	}

	public void setLayoutType(String layoutType) {
		this.layoutType = layoutType;
	}

}
