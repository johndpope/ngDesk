package com.ngdesk.company.module.dao;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CreateLayoutTemplate {

	@JsonProperty("CREATE_LAYOUTS")
	@Field("CREATE_LAYOUTS")
	private List<CreateEditLayout> createLayout = new ArrayList<CreateEditLayout>();

	@JsonProperty("TIER")
	@Field("TIER")
	private String tier = "free";

	@JsonProperty("MODULE_ID")
	@Field("MODULE_ID")
	private String moduleId;

	public CreateLayoutTemplate() {
		super();
	}

	public CreateLayoutTemplate(List<CreateEditLayout> createLayout, String tier, String moduleId) {
		super();
		this.createLayout = createLayout;
		this.tier = tier;
		this.moduleId = moduleId;
	}

	public List<CreateEditLayout> getCreateLayout() {
		return createLayout;
	}

	public void setCreateLayout(List<CreateEditLayout> createLayout) {
		this.createLayout = createLayout;
	}

	public String getTier() {
		return tier;
	}

	public void setTier(String tier) {
		this.tier = tier;
	}

	public String getModuleId() {
		return moduleId;
	}

	public void setModuleId(String moduleId) {
		this.moduleId = moduleId;
	}

}
