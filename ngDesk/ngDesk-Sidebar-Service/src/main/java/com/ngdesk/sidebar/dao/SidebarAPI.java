package com.ngdesk.sidebar.dao;

import java.util.List;
import java.util.Optional;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.ngdesk.commons.exceptions.NotFoundException;
import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.repositories.SidebarRepository;

import io.swagger.v3.oas.annotations.Operation;

@RestController
@RefreshScope
public class SidebarAPI {
	
	@Autowired
	private SidebarRepository sidebarRepository;

	@Autowired
	private AuthManager authManager;
	
	@GetMapping("/companies/sidebar")
	@Operation(summary = "Get Sidebar by Company", description = "Gets the company sidebar")
	public Sidebar getSidebar() {
		String companyId = authManager.getUserDetails().getCompanyId();
		Optional<CustomSidebar> customSidebar = sidebarRepository.findCustomSidebarByCompanyId("companies_sidebar", companyId);
		if (customSidebar.isEmpty()) {
			String vars[] = { "Custom sidebar" };
			throw new NotFoundException("DAO_NOT_FOUND", vars);
		}
		return customSidebar.get().getSidebar();
	}
	
	@GetMapping("/companies/sidebar/role")
	@Operation(summary = "Get Sidebar by Role", description = "Gets the company sidebar by specified role")
	public Menu getSidebarByRole() {
		System.out.println("inside the api");
		String companyId = authManager.getUserDetails().getCompanyId();
		Optional<CustomSidebar> customSidebar = sidebarRepository.findCustomSidebarByCompanyId("companies_sidebar", companyId);
		if (customSidebar.isEmpty()) {
			String vars[] = { "Custom sidebar" };
			throw new NotFoundException("DAO_NOT_FOUND", vars);
		}
		String roleId = authManager.getUserDetails().getRole();
		List<Menu> menus = customSidebar.get().getSidebar().getSidebarMenu();
		for (Menu menu: menus) {
			if (menu.getRole().equals(roleId)) {
				return menu;
			}
		}
		return new Menu();
	}
	
	@PutMapping("/companies/sidebar")
	@Operation(summary = "Put Sidebar", description = "Update a sidebar")
	public Sidebar putSidebar(@Valid @RequestBody Sidebar sidebar) {

		String companyId = authManager.getUserDetails().getCompanyId();
		Optional<CustomSidebar> optional = sidebarRepository.findCustomSidebarByCompanyId("companies_sidebar", companyId);
		if (optional.isEmpty()) {
			String vars[] = { "Custom sidebar" };
			throw new NotFoundException("DAO_NOT_FOUND", vars);
		}
		
		CustomSidebar customSidebar = optional.get();
		customSidebar.setSidebar(sidebar);
		sidebarRepository.save(customSidebar, "companies_sidebar");

		// TODO: Check validations
		return sidebar;

	}

}
