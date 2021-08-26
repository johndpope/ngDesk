package com.ngdesk.company.role.dao;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.elasticsearch.index.fielddata.plain.SortedNumericDVIndexFieldData.NanoSecondFieldData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ngdesk.commons.Global;
import com.ngdesk.commons.exceptions.BadRequestException;
import com.ngdesk.company.dao.Company;
import com.ngdesk.company.module.dao.Module;
import com.ngdesk.company.module.dao.ModuleField;
import com.ngdesk.company.module.dao.ModuleService;
import com.ngdesk.company.plugin.dao.Plugin;
import com.ngdesk.company.plugin.dao.Tier;
import com.ngdesk.company.plugin.dao.TierModule;
import com.ngdesk.company.rolelayout.dao.RoleLayout;
import com.ngdesk.company.rolelayout.dao.Tab;
import com.ngdesk.repositories.PluginRepository;
import com.ngdesk.repositories.RoleLayoutRepository;
import com.ngdesk.repositories.RoleRepository;

@Component
public class RoleService {

	@Autowired
	ModuleService moduleService;

	@Autowired
	Global global;

	@Autowired
	RoleRepository roleRepository;

	@Autowired
	RoleLayoutRepository roleLayoutRepository;

	@Autowired
	PluginRepository pluginRepository;

	public Map<String, String> postDefaultRoles(Company company) {
		Map<String, String> rolesMap = new HashMap<String, String>();
		try {
			ObjectMapper mapper = new ObjectMapper();
			ArrayList<String> roleNames = new ArrayList<String>();
			roleNames.add("Agent");
			roleNames.add("SystemAdmin");
			roleNames.add("Customers");
			roleNames.add("Sales");
			if (company.getPlugins().contains("Expenses")) {
				roleNames.add("Accountant");
				roleNames.add("Spender");
				roleNames.add("AccountingManager");
			}
			for (String roleName : roleNames) {
				String roleJson = global.getFile(roleName + "Role.json");

				for (String moduleName : moduleService.modulesMap.keySet()) {
					String name = moduleName.replaceAll("\\s+", "_").toUpperCase() + "_REPLACE";
					roleJson = roleJson.replaceAll(name, moduleService.modulesMap.get(moduleName).toString());

				}
				Role role = mapper.readValue(roleJson, Role.class);

				for (String moduleName : moduleService.modulesMap.keySet()) {
					String moduleId = moduleService.modulesMap.get(moduleName).toString();
					String moduleJson = global.getFile(moduleName.replaceAll("\\s+", "") + "Module.json");
					SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
					moduleJson = moduleJson.replaceAll("DATE_CREATED_REPLACE", format.format(new Date()));
					moduleJson = moduleJson.replaceAll("DATE_REPLACE", format.format(new Date()));
					if (moduleJson != null) {
						Module module = mapper.readValue(moduleJson, Module.class);
						List<ModuleField> fields = module.getFields();
						List<FieldPermission> allFieldPermissions = new ArrayList<FieldPermission>();

						List<Permission> permissions = role.getPermissions();
						Permission permission = permissions.stream().filter(perm -> perm.getModule().equals(moduleId))
								.findFirst().orElse(null);

						if (permission != null) {
							List<String> fieldIds = new ArrayList<String>();
							List<FieldPermission> fieldPermissions = permission.getFieldPermissions();
							fieldPermissions.forEach(fieldPermission -> {
								if (fieldPermission != null) {
									ModuleField moduleField = fields.stream()
											.filter(field -> field.getFieldId().equals(fieldPermission.getFieldId()))
											.findFirst().orElse(null);

									if (moduleField == null || fieldIds.contains(moduleField.getFieldId())) {
										return;
									} else {
										allFieldPermissions.add(fieldPermission);
										fieldIds.add(fieldPermission.getFieldId());
									}
								}
							});
							fields.forEach(field -> {

								if (!fieldIds.contains(field.getFieldId())) {
									FieldPermission fieldPermissionNotPresent = new FieldPermission(field.getFieldId(),
											"Not Set");
									allFieldPermissions.add(fieldPermissionNotPresent);
								}
							});
							permission.setFieldPermissions(allFieldPermissions);
						}

						role.setPermissions(permissions);
					}
				}

				roleRepository.save(role, "roles_" + company.getCompanyId());
				rolesMap.put(role.getName(), role.getId());
			}

			return rolesMap;
		} catch (Exception e) {
			e.printStackTrace();
		}
		throw new BadRequestException("ROLE_POST_FAILED", null);
	}

	public void postDefaultRoleLayouts(Company company, Map<String, String> rolesMap) {
		try {
			String companyId = company.getCompanyId();
			String[] roleNames = { "Agent", "SystemAdmin" };
			for (String roleName : roleNames) {
				String roleLayoutJson = global.getFile(roleName + "RoleLayout.json");
				roleLayoutJson = roleLayoutJson.replaceAll("COMPANY_ID_REPLACE", companyId);

				// ROLE NEEDS TO BE REPLACED WITH ROLE ID FOR DEFAULT LAYOUTS.
				String roleId = rolesMap.get(roleName);
				String replaceName = roleName.toUpperCase().replaceAll("\\s+", "_") + "_ROLE_REPLACE";
				roleLayoutJson = roleLayoutJson.replaceAll(replaceName, roleId);

				for (String moduleName : moduleService.modulesMap.keySet()) {
					String moduleId = moduleService.modulesMap.get(moduleName).toString();
					String name = moduleName.toUpperCase().replaceAll("\\s+", "_") + "_MODULE_ID_REPLACE";
					roleLayoutJson = roleLayoutJson.replaceAll(name, moduleId);
				}
				RoleLayout layout = new ObjectMapper().readValue(roleLayoutJson, RoleLayout.class);

				for (Tab tab : layout.getTabs()) {
					tab.setTabId(UUID.randomUUID().toString());
				}
				roleLayoutRepository.save(layout, "role_layouts");
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new BadRequestException("ROLE_LAYOUTS_POST_FAILED", null);
		}
	}

}
