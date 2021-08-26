package com.ngdesk.module.mobile.layout.dao;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ngdesk.module.layout.dao.CreateEditLayout;

public class EditMobileLayoutTemplate {

	@JsonProperty("EDIT_MOBILE_LAYOUTS")
	@Field("EDIT_MOBILE_LAYOUTS")
	private List<CreateEditMobileLayout> editMobileLayout = new ArrayList<CreateEditMobileLayout>();

	@JsonProperty("TIER")
	@Field("TIER")
	private String tier = "free";

	@JsonProperty("MODULE_ID")
	@Field("MODULE_ID")
	private String moduleId;

	public EditMobileLayoutTemplate() {
		super();
	}

	public EditMobileLayoutTemplate(List<CreateEditMobileLayout> editMobileLayout, String tier, String moduleId) {
		super();
		this.editMobileLayout = editMobileLayout;
		this.tier = tier;
		this.moduleId = moduleId;
	}

	public List<CreateEditMobileLayout> getEditMobileLayout() {
		return editMobileLayout;
	}

	public void setEditMobileLayout(List<CreateEditMobileLayout> editMobileLayout) {
		this.editMobileLayout = editMobileLayout;
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
