package com.ngdesk.module.mobile.layout.dao;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ListMobileLayoutTemplate {

	@Field("LIST_MOBILE_LAYOUTS")
	@JsonProperty("LIST_MOBILE_LAYOUTS")
	private List<ListMobileLayout> listMobileLayouts = new ArrayList<ListMobileLayout>();

	@JsonProperty("TIER")
	@Field("TIER")
	private String tier = "free";

	@JsonProperty("MODULE_ID")
	@Field("MODULE_ID")
	private String moduleId;

	public ListMobileLayoutTemplate() {
		super();
	}

	public ListMobileLayoutTemplate(List<ListMobileLayout> listMobileLayouts, String tier, String moduleId) {
		super();
		this.listMobileLayouts = listMobileLayouts;
		this.tier = tier;
		this.moduleId = moduleId;
	}

	public List<ListMobileLayout> getListMobileLayouts() {
		return listMobileLayouts;
	}

	public void setListMobileLayouts(List<ListMobileLayout> listMobileLayouts) {
		this.listMobileLayouts = listMobileLayouts;
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
