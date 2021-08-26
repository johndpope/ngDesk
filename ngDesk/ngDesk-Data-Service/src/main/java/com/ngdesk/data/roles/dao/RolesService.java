package com.ngdesk.data.roles.dao;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.data.dao.DataService;
import com.ngdesk.data.modules.dao.ModuleService;
import com.ngdesk.repositories.module.entry.ModuleEntryRepository;
import com.ngdesk.repositories.module.entry.ModulesRepository;
import com.ngdesk.repositories.roles.RolesRepository;

@Component
public class RolesService {

	@Autowired
	RolesRepository rolesRepository;

	@Autowired
	AuthManager authManager;

	@Autowired
	ModulesRepository modulesRepository;

	@Autowired
	ModuleEntryRepository moduleEntryRepository;

	@Autowired
	ModuleService moduleService;

	@Autowired
	DataService dataService;

	public boolean isAuthorizedForRecord(String roleId, String requestType, String moduleId) {

		Assert.notNull(roleId, "Role ID is required");
		Assert.notNull(requestType, "Request type is required");
		Assert.notNull(moduleId, "Module ID is required");

		try {
			Optional<Role> optionalRole = rolesRepository.findById(roleId,
					"roles_" + authManager.getUserDetails().getCompanyId());
			if (optionalRole.isEmpty()) {
				return false;
			}

			Role role = optionalRole.get();
			if (role.getName().equals("SystemAdmin")) {
				return true;
			}

			Permission modulePermission = role.getPermissions().stream()
					.filter(permission -> permission.getModule().equals(moduleId)).findFirst().orElse(null);

			if (modulePermission == null) {
				return false;
			}

			ModuleLevelPermission moduleLevelPermission = modulePermission.getModulePermission();
			if (moduleLevelPermission.getAccess().equalsIgnoreCase("disabled")) {
				return false;
			} else {
				if (requestType.equals("GET")) {
					if (moduleLevelPermission.getView().equals("None")) {
						return false;
					}
				} else if (requestType.equals("POST") || requestType.equals("PUT")) {
					if (moduleLevelPermission.getEdit().equals("None")) {
						return false;
					}
				} else if (requestType.equals("DELETE")) {
					if (moduleLevelPermission.getDelete().equals("None")) {
						return false;
					}
				} else {
					return false;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public boolean isSystemAdmin(String roleId) {
		Assert.notNull(roleId, "Role Id is required");
		Optional<Role> optionalRole = rolesRepository.findById(roleId,
				"roles_" + authManager.getUserDetails().getCompanyId());
		if (optionalRole.isEmpty()) {
			return false;
		}

		Role role = optionalRole.get();
		if (role.getName().equals("SystemAdmin")) {
			return true;
		}
		return false;
	}

	public boolean isCustomer(String roleId) {
		Assert.notNull(roleId, "Role Id is required");
		Optional<Role> optionalRole = rolesRepository.findById(roleId,
				"roles_" + authManager.getUserDetails().getCompanyId());
		if (optionalRole.isEmpty()) {
			return false;
		}

		Role role = optionalRole.get();
		if (role.getName().equals("Customers")) {
			return true;
		}
		return false;

	}

}
