package com.ngdesk.company.sidebar.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ngdesk.commons.Global;
import com.ngdesk.commons.exceptions.BadRequestException;
import com.ngdesk.company.dao.Company;
import com.ngdesk.company.module.dao.ModuleService;
import com.ngdesk.company.plugin.dao.Plugin;
import com.ngdesk.repositories.PluginRepository;
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
	PluginRepository pluginRepository;

	public void postDefaultSidebar(Company company, Map<String, String> rolesMap) {

		try {
			Sidebar sidebar = new Sidebar();
			List<String> userPlugins = company.getPlugins();
			List<Menu> menus = new ArrayList<Menu>();
			List<String> menuNamesToBeRemoved = new ArrayList<String>();
			Optional<List<Plugin>> optionalPlugins = pluginRepository.findAllPlugins();
			if (optionalPlugins.isPresent()) {
				for (Plugin plugin : optionalPlugins.get()) {
					if (!userPlugins.contains(plugin.getName()) && !plugin.getName().equals("Ticketing")
							&& !plugin.getName().equals("Pager")) {
						menuNamesToBeRemoved.add(plugin.getName());
					}
				}
				for (String roleName : rolesMap.keySet()) {
					String menuJson = global.getFile(roleName.replaceAll("\\s", "") + "Sidebar.json");
					String roleId = rolesMap.get(roleName);
					roleName = roleName.toUpperCase().replaceAll("\\s+", "_");
					menuJson = menuJson.replaceAll(roleName + "_ROLE_REPLACE", roleId);

					for (String name : moduleService.modulesMap.keySet()) {
						String moduleId = moduleService.modulesMap.get(name).toString();
						name = name.toUpperCase().replaceAll("\\s+", "_");
						menuJson = menuJson.replaceAll(name.toUpperCase() + "_ID_REPLACE", moduleId);
					}

					Menu menu = new ObjectMapper().readValue(menuJson, Menu.class);

					List<MenuItem> menuItemsToBeRemoved = new ArrayList<MenuItem>();
					if (menu.getMenuItems() != null) {
						for (MenuItem menuItem : menu.getMenuItems()) {
							if (menuItem.getName() != null && menuNamesToBeRemoved.contains(menuItem.getName())) {
								menuItemsToBeRemoved.add(menuItem);

							}
						}
					}

					menu.getMenuItems().removeAll(menuItemsToBeRemoved);
					menus.add(menu);
				}

				sidebar.setSidebarMenu(menus);
				CustomSidebar customSidebar = new CustomSidebar();
				customSidebar.setCompanyId(company.getCompanyId());
				customSidebar.setSidebar(sidebar);
				sidebarRepository.save(customSidebar, "companies_sidebar");
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new BadRequestException("SIDEBAR_POST_FAILED", null);
		}
	}

}
