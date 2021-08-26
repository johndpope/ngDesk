package com.ngdesk.module.mobile.layout.dao;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CreateMobileLayoutTemplate {

	@JsonProperty("CREATE_MOBILE_LAYOUTS")
	@Field("CREATE_MOBILE_LAYOUTS")
	private List<CreateEditMobileLayout> createMobileLayout = new ArrayList<CreateEditMobileLayout>();

	@JsonProperty("TIER")
	@Field("TIER")
	private String tier = "free";

	@JsonProperty("MODULE_ID")
	@Field("MODULE_ID")
	private String moduleId;

	public CreateMobileLayoutTemplate() {
		super();
	}

	public CreateMobileLayoutTemplate(List<CreateEditMobileLayout> createMobileLayout, String tier, String moduleId) {
		super();
		this.createMobileLayout = createMobileLayout;
		this.tier = tier;
		this.moduleId = moduleId;
	}

	public List<CreateEditMobileLayout> getCreateMobileLayout() {
		return createMobileLayout;
	}

	public void setCreateMobileLayout(List<CreateEditMobileLayout> createMobileLayout) {
		this.createMobileLayout = createMobileLayout;
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
