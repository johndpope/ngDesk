package com.ngdesk.company.sidebar.dao;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ngdesk.commons.Global;
import com.ngdesk.commons.exceptions.BadRequestException;
import com.ngdesk.company.dao.Company;
import com.ngdesk.company.module.dao.ModuleService;
import com.ngdesk.repositories.SidebarRepository;

@Component
public class SidebarService {

	@Autowired
	SidebarRepository sidebarRepository;

	@Autowired
	Global global;

	@Autowired
	ModuleService moduleService;

	public void postDefaultSidebar(Company company, Map<String, String> rolesMap) {

		try {
			String sidebarJson = global.getFile("default-sidebar.json");
			if (company.getCompanySubdomain().equals("ngdesk-sam")) {
				sidebarJson = global.getFile("sidebar-sam.json");
			} else if (company.getCompanySubdomain().equals("ngdesk-crm")) {
				sidebarJson = global.getFile("sidebar-crm.json");
			} else if (company.getPlugins().contains("Expenses")) {
				sidebarJson = global.getFile("sidebar-expenses.json");
			}
			for (String name : moduleService.modulesMap.keySet()) {
				String moduleId = moduleService.modulesMap.get(name).toString();
				name = name.toUpperCase().replaceAll("\\s+", "_");
				sidebarJson = sidebarJson.replaceAll(name.toUpperCase() + "_ID_REPLACE", moduleId);
			}

			for (String name : rolesMap.keySet()) {
				String roleId = rolesMap.get(name);
				name = name.toUpperCase().replaceAll("\\s+", "_");
				sidebarJson = sidebarJson.replaceAll(name + "_ROLE_REPLACE", roleId);
			}

			Sidebar sidebar = new ObjectMapper().readValue(sidebarJson, Sidebar.class);
			CustomSidebar customSidebar = new CustomSidebar();
			customSidebar.setCompanyId(company.getCompanyId());
			customSidebar.setSidebar(sidebar);

			sidebarRepository.save(customSidebar, "companies_sidebar");

		} catch (Exception e) {
			e.printStackTrace();
			throw new BadRequestException("SIDEBAR_POST_FAILED", null);
		}
	}
}
