package com.ngdesk.sidebar.dao;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertEvent;
import org.springframework.stereotype.Component;

import com.ngdesk.commons.exceptions.BadRequestException;
import com.ngdesk.commons.exceptions.NotFoundException;
import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.repositories.SidebarRepository;

@Component
public class BeforeSaveListener extends AbstractMongoEventListener<CustomSidebar> {

	@Autowired
	SidebarRepository sidebarRespository;

	@Autowired
	AuthManager authManager;

	@Override
	public void onBeforeConvert(BeforeConvertEvent<CustomSidebar> event) {
		CustomSidebar customSidebar = event.getSource();
		String companyId = authManager.getUserDetails().getCompanyId();

		Sidebar sidebar = customSidebar.getSidebar();
		for (Menu menu : sidebar.getSidebarMenu()) {
			// CHECK IF ROLE EXISTS
			String role = menu.getRole();
			if (sidebarRespository.findById(role, "roles_" + companyId).isEmpty()) {
				String vars[] = { "ROLE" };
				throw new NotFoundException("DAO_NOT_FOUND", vars);
			}

			// DUPLICATE MODULE CHECK FOR EACH SIDEBAR
			List<String> duplicateModuleCheck = new ArrayList<String>();
			for (MenuItem menuItem : menu.getMenuItems()) {
				// IF_MODULE IS SPECIFIED, MODULE MUST NOT BE NULL AND VALID
				if (menuItem.getIsModule()) {
					if (!duplicateModuleCheck.contains(menuItem.getModule())) {
						duplicateModuleCheck.add(menuItem.getModule());
					} else {
						String vars[] = {};
						throw new BadRequestException("DUPLICATE_MODULE_MENU", vars);
					}

					String moduleId = menuItem.getModule();
					if (sidebarRespository.findById(moduleId, "modules_" + companyId).isEmpty()) {
						String vars[] = { "MODULE" };
						throw new NotFoundException("DAO_NOT_FOUND", vars);
					} else {
						// ENSURE THAT MODULE MENUS DO NOT HAVE SUB MENUS
						if (menuItem.getSubMenuItems().size() > 0) {
							throw new BadRequestException("MODULE_MENU_CANNOT_HAVE_SUBMENU_ITEMS", null);
						}
					}
				} else {
					for (SubMenuItem subMenuItem : menuItem.getSubMenuItems()) {
						if (subMenuItem.isModule()) {
							if (!duplicateModuleCheck.contains(subMenuItem.getModule())) {
								duplicateModuleCheck.add(subMenuItem.getModule());
							} else {
								String vars[] = {};
								throw new BadRequestException("DUPLICATE_MODULE_SUB_MENU", vars);
							}
						}
					}
				}
			}
		}

	}

}
