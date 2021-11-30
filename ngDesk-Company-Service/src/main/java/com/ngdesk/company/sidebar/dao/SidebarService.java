package com.ngdesk.company.sidebar.dao;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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

	@SuppressWarnings("unlikely-arg-type")
	public void postDefaultSidebar(Company company, Map<String, String> rolesMap) {

		try {
			String sidebarJson = global.getFile("Sidebar.json");
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
			List<String> userPlugins = company.getPlugins();

			sidebar = removeUnwantedMenuItems(rolesMap, sidebar, userPlugins);

			CustomSidebar customSidebar = new CustomSidebar();
			customSidebar.setCompanyId(company.getCompanyId());
			customSidebar.setSidebar(sidebar);
			sidebarRepository.save(customSidebar, "companies_sidebar");

		} catch (Exception e) {
			e.printStackTrace();
			throw new BadRequestException("SIDEBAR_POST_FAILED", null);
		}
	}

	public Sidebar removeUnwantedMenuItems(Map<String, String> rolesMap, Sidebar sidebar, List<String> userPlugins) {

		for (String roleName : rolesMap.keySet()) {
			List<String> menuNamesToBeRemoved = new ArrayList<String>();
			if (roleName.equals("SystemAdmin")) {
				String plugins[] = { "CRM", "Expenses", "SAM", "Chats", "Change Requests",
						"Human Resource Management" };
				for (String plugin : plugins) {
					if (!userPlugins.contains(plugin)) {
						menuNamesToBeRemoved.add(plugin);
					}

					if (sidebar.getSidebarMenu() != null) {
						for (Menu menu : sidebar.getSidebarMenu()) {
							List<MenuItem> menuItemsToBeRemoved = new ArrayList<MenuItem>();
							if (menu.getRole().equals(rolesMap.get(roleName))) {
								if (menu.getMenuItems() != null) {
									for (MenuItem menuItem : menu.getMenuItems()) {
										if (menuItem.getName() != null
												&& menuNamesToBeRemoved.contains(menuItem.getName())) {
											menuItemsToBeRemoved.add(menuItem);

										}
									}
								}
							}
							menu.getMenuItems().removeAll(menuItemsToBeRemoved);
						}
					}
				}
			}
			if (roleName.equals("Agent") && !userPlugins.contains("Chats")) {
				if (sidebar.getSidebarMenu() != null) {
					Optional<Menu> optionalAgentMenu = sidebar.getSidebarMenu().stream()
							.filter(menu -> menu.getRole().equals(rolesMap.get(roleName))).findFirst();
					if (optionalAgentMenu.isPresent()) {
						Optional<MenuItem> optionalChatsMenuItem = optionalAgentMenu.get().getMenuItems().stream()
								.filter(menuItem -> menuItem.getName().equals("Chats")).findFirst();
						if (optionalChatsMenuItem.isPresent()) {
							List<MenuItem> menuItemsToBeRemoved = new ArrayList<MenuItem>();
							menuItemsToBeRemoved.add(optionalChatsMenuItem.get());
							optionalAgentMenu.get().getMenuItems().removeAll(menuItemsToBeRemoved);
						}
					}
				}

			}
		}
		if (!userPlugins.contains("Expenses")) {
			String rolesToBeRemoved[] = { "ACCOUNTANT_ROLE_REPLACE", "SPENDER_ROLE_REPLACE",
					"ACCOUNTING_MANAGER_ROLE_REPLACE" };
			List<Menu> menusToBeRemoved = new ArrayList<Menu>();
			for (Menu menu : sidebar.getSidebarMenu()) {
				if (Arrays.asList(rolesToBeRemoved).contains(menu.getRole())) {
					menusToBeRemoved.add(menu);
				}
			}
			sidebar.getSidebarMenu().removeAll(menusToBeRemoved);
		}

		return sidebar;
	}

}
