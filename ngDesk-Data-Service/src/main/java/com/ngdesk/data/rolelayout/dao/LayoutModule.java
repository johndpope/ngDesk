package com.ngdesk.data.rolelayout.dao;

import javax.validation.Valid;

import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ngdesk.commons.annotations.CustomNotEmpty;

import io.swagger.v3.oas.annotations.media.Schema;

public class LayoutModule {

	@JsonProperty("MODULE")
	@Field("MODULE")
	private String module;

	@JsonProperty("LIST_LAYOUT")
	@Field("LIST_LAYOUT")
	@Valid
	private Layout layout;

	public LayoutModule(String module, @Valid Layout layout) {
		super();
		this.module = module;
		this.layout = layout;
	}

	public String getModule() {
		return module;
	}

	public void setModule(String module) {
		this.module = module;
	}

	public Layout getLayout() {
		return layout;
	}

	public void setLayout(Layout layout) {
		this.layout = layout;
	}

	public LayoutModule() {
	}

}
