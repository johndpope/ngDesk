package com.ngdesk.company.sidebar.dao;

import java.util.ArrayList;
import java.util.List;
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

	@Autowired
	MenuItem menuItem;

	@SuppressWarnings("unlikely-arg-type")
	public void postDefaultSidebar(Company company, Map<String, String> rolesMap) {

//		try {
//			String defaultsidebarJson = global.getFile("default-sidebar.json");
//			if (company.getCompanySubdomain().equals("ngdesk-sam")) {
//				defaultsidebarJson = global.getFile("sidebar-sam.json");
//			} else if (company.getCompanySubdomain().equals("ngdesk-crm")) {
//				defaultsidebarJson = global.getFile("sidebar-crm.json");
//			} else if (company.getPlugins().contains("Expenses")) {
//				defaultsidebarJson = global.getFile("sidebar-expenses.json");
//			}
//
//			for (String name : moduleService.modulesMap.keySet()) {
//				String moduleId = moduleService.modulesMap.get(name).toString();
//				name = name.toUpperCase().replaceAll("\\s+", "_");
//				defaultsidebarJson = defaultsidebarJson.replaceAll(name.toUpperCase() + "_ID_REPLACE", moduleId);
//			}
//
//			for (String name : rolesMap.keySet()) {
//				String roleId = rolesMap.get(name);
//				name = name.toUpperCase().replaceAll("\\s+", "_");
//				defaultsidebarJson = defaultsidebarJson.replaceAll(name + "_ROLE_REPLACE", roleId);
//			}
//
//			Sidebar sidebar = new ObjectMapper().readValue(defaultsidebarJson, Sidebar.class);
//			CustomSidebar customSidebar = new CustomSidebar();
//			customSidebar.setCompanyId(company.getCompanyId());
//			customSidebar.setSidebar(sidebar);
//
//			sidebarRepository.save(customSidebar, "companies_sidebar");
//
//		} catch (Exception e) {
//			e.printStackTrace();
//			throw new BadRequestException("SIDEBAR_POST_FAILED", null);
//		}
		try {
			String sidebarJson = global.getFile("sidebar.json");
			for (String name : rolesMap.keySet()) {
				String roleId = rolesMap.get(name);
				name = name.toUpperCase().replaceAll("\\s+", "_");
				sidebarJson = sidebarJson.replaceAll(name + "_ROLE_REPLACE", roleId);
			}
			for (String name : moduleService.modulesMap.keySet()) {
				String moduleId = moduleService.modulesMap.get(name).toString();
				name = name.toUpperCase().replaceAll("\\s+", "_");
				sidebarJson = sidebarJson.replaceAll(name.toUpperCase() + "_ID_REPLACE", moduleId);
			}
			Sidebar sidebar = new ObjectMapper().readValue(sidebarJson, Sidebar.class);
			List<String> menuItemsToBeRemoved = new ArrayList<String>();
			List<Menu> menuItem = sidebar.getSidebarMenu();
			List<String> userPlugins = company.getPlugins();
			String[] pluginNames = new String[] { "Ticketing", "CRM", "Expenses", "Software Asset Management", "Chats",
					"Change Requests", "Pager", "Human Resource Management" };
			for (String pluginName : pluginNames) {
				if (!(userPlugins.contains(pluginName))) {

					menuItemsToBeRemoved.add(pluginName);
				}
			}
			for (String menuItemToBeRemoved : menuItemsToBeRemoved) {
				if (menuItem.contains(menuItemToBeRemoved)) {
					menuItem.remove(menuItemToBeRemoved);					
				}
			}
			sidebar.setSidebarMenu(menuItem);

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
