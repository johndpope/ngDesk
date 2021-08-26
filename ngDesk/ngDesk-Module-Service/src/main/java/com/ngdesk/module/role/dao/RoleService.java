package com.ngdesk.module.role.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.ngdesk.commons.exceptions.BadRequestException;
import com.ngdesk.commons.exceptions.ForbiddenException;
import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.module.dao.Module;
import com.ngdesk.module.field.dao.ModuleField;
import com.ngdesk.repositories.RoleRepository;

@Component
public class RoleService {

	@Autowired
	AuthManager authManager;

	@Autowired
	RoleRepository rolesRepository;

	public void isValidRoleId(String roleId, String collectionName) {

		Optional<Role> optionalrole = rolesRepository.findById(roleId, collectionName);

		if (optionalrole.isEmpty()) {
			throw new BadRequestException("ROLE_ID_INVALID", null);
		}
	}

	public boolean isAuthorized(String roleId, String requestType, String moduleId) {

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

	public boolean isSystemAdmin() {
		Optional<Role> optionalRole = rolesRepository.findById(authManager.getUserDetails().getRole(),
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

	public void updateRolesWithNewField(Module module, ModuleField field) {
		String companyId = authManager.getUserDetails().getCompanyId();
		List<Role> roles = rolesRepository.findAll(new Query(), "roles_" + companyId);

		roles.forEach(role -> {
			Permission modulePermission = role.getPermissions().stream()
					.filter(permission -> permission.getModule().equals(module.getModuleId())).findFirst().orElse(null);
			if (modulePermission != null) {
				FieldPermission fieldPermission = new FieldPermission();
				fieldPermission.setFieldId(field.getFieldId());
				fieldPermission.setPermission("Not Set");

				List<FieldPermission> fieldPermissions = new ArrayList<FieldPermission>();
				if (modulePermission.getFieldPermissions() == null) {
					modulePermission.setFieldPermissions(fieldPermissions);
				}
				modulePermission.getFieldPermissions().add(fieldPermission);

			}
			rolesRepository.save(role, "roles_" + companyId);
		});
	}

	public void postNewRole(Role role, List<Module> allModules) {
		List<Permission> permissions = role.getPermissions();
		for (Permission permission : permissions) {
			for (Module module : allModules) {
				permission.setModule(module.getModuleId());
				ModuleLevelPermission modulePermission = permission.getModulePermission();
				modulePermission.setAccess("Enabled");
				modulePermission.setView("All");
				modulePermission.setEdit("All");
				modulePermission.setAccessType("Not Set");
				modulePermission.setDelete("None");
			}
		}

	}

}
