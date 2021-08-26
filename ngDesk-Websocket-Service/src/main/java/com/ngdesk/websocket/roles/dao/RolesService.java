package com.ngdesk.websocket.roles.dao;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.ngdesk.commons.exceptions.BadRequestException;
import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.websocket.modules.dao.Module;
import com.ngdesk.repositories.ModulesRepository;
import com.ngdesk.repositories.RolesRepository;

@Component
public class RolesService {

	@Autowired
	RolesRepository rolesRepository;

	@Autowired
	ModulesRepository modulesRepository;

	public boolean isAuthorizedForRecord(String roleId, String requestType, String moduleId, String companyId) {

		Assert.notNull(roleId, "Role ID is required");
		Assert.notNull(requestType, "Request type is required");
		Assert.notNull(moduleId, "Module ID is required");

		try {
			Optional<Role> optionalRole = rolesRepository.findById(roleId,
					"roles_" + companyId);
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

	public boolean isAuthorized(String companyId, String roleId, String moduleId, Map<String, Object> entry) {

		Assert.notNull(roleId, "Role ID is required");
		Assert.notNull(entry, "Entry is required");
		Assert.notNull(moduleId, "Module ID is required");

		try {
			Optional<Role> optionalRole = rolesRepository.findById(roleId,
					"roles_" + companyId);
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

			Optional<Module> optionalModule = modulesRepository.findById(moduleId,
					"modules_" + companyId);
			if (optionalModule.isEmpty()) {
				return false;
			}

			Module module = optionalModule.get();
			Map<String, String> fieldsMap = new HashMap<String, String>();
			module.getFields().forEach(field -> {
				fieldsMap.put(field.getFieldId(), field.getName());
			});

			modulePermission.getFieldPermissions().forEach(fieldPermission -> {
				if (fieldPermission.getPermission().equalsIgnoreCase("Read")) {
					String fieldName = fieldsMap.get(fieldPermission.getFieldId());
					if (entry.containsKey(fieldName)) {
						String[] vars = { fieldName };
						throw new BadRequestException("RESTRICTED_FIELD", vars);
					}
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}

	public boolean isSystemAdmin(String companyId, String roleId) {
		Assert.notNull(roleId, "Role Id is required");
		Optional<Role> optionalRole = rolesRepository.findById(roleId,
				"roles_" +companyId);
		if (optionalRole.isEmpty()) {
			return false;
		}

		Role role = optionalRole.get();
		if (role.getName().equals("SystemAdmin")) {
			return true;
		}
		return false;
	}
	
	public boolean isCustomer(String companyId, String roleId) {
		Assert.notNull(roleId, "Role Id is required");
		Optional<Role> optionalRole = rolesRepository.findById(roleId,
				"roles_" + companyId);
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
