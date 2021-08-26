package com.ngdesk.module.field.dao;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ngdesk.module.dao.Module;

public class ModuleInfo {

	@JsonProperty("MODULE")
	private Module module;

	@JsonProperty("COMPANY_ID")
	private String companyId;

	@JsonProperty("TIER")
	private String tier;

	@JsonProperty("SELECTED_MODULES")
	private List<String> modules;

	public ModuleInfo(Module module, String companyId, String tier, List<String> modules) {
		super();
		this.module = module;
		this.companyId = companyId;
		this.tier = tier;
		this.modules = modules;
	}

	public Module getModule() {
		return module;
	}

	public void setModule(Module module) {
		this.module = module;
	}

	public String getCompanyId() {
		return companyId;
	}

	public void setCompanyId(String companyId) {
		this.companyId = companyId;
	}

	public String getTier() {
		return tier;
	}

	public void setTier(String tier) {
		this.tier = tier;
	}

	public List<String> getModules() {
		return modules;
	}

	public void setModules(List<String> modules) {
		this.modules = modules;
	}

}
