package com.ngdesk.graphql.role.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.graphql.modules.dao.Module;
import com.ngdesk.graphql.modules.dao.ModuleField;
import com.ngdesk.repositories.modules.ModulesRepository;
import com.ngdesk.repositories.role.RolesRepository;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

@Component
public class FieldPermissionDataFetcher implements DataFetcher<List<EditablePermission>> {

	@Autowired
	AuthManager authManager;

	@Autowired
	RolesRepository roleRepository;

	@Autowired
	ModulesRepository moduleRepository;

	@Autowired
	FieldPermissionService fieldPermissionService;

	@Override
	public List<EditablePermission> get(DataFetchingEnvironment environment) throws Exception {

		String moduleId = environment.getArgument("moduleId");
		String layout = environment.getArgument("layout");
		String dataId = environment.getArgument("dataId");
		String roleId = authManager.getUserDetails().getRole();
		Optional<Role> optionalRole = roleRepository.findById(roleId,
				"roles_" + authManager.getUserDetails().getCompanyId());

		if (optionalRole.isEmpty()) {
			return null;
		}

		Optional<Module> optionalModule = moduleRepository.findById(moduleId,
				"modules_" + authManager.getUserDetails().getCompanyId());

		if (optionalModule.isEmpty()) {
			return null;
		}

		Role role = optionalRole.get();
		Module module = optionalModule.get();
		List<Permission> permissions = role.getPermissions();
		Permission permission = permissions.stream()
				.filter(permissionObject -> permissionObject.getModule().equals(moduleId)).findFirst().orElse(null);
 
		if (permission == null && !role.getName().equals("SystemAdmin")) {
			return null;
		}

		List<EditablePermission> fieldPermissionsList = new ArrayList<EditablePermission>();
		if (role.getName().equals("SystemAdmin")) {
			for (ModuleField field : module.getFields()) {
				EditablePermission editablePermission = new EditablePermission();
				editablePermission.setFieldId(field.getFieldId());
				editablePermission.setNotEditable(false);
				fieldPermissionsList.add(editablePermission);
			}
		} else {
			if (permission.getModulePermission().getAccess().equalsIgnoreCase("Disabled")) {
				for (ModuleField field : module.getFields()) {
					EditablePermission editablePermission = new EditablePermission();
					editablePermission.setFieldId(field.getFieldId());
					editablePermission.setNotEditable(true);
					fieldPermissionsList.add(editablePermission);
				}
			} else {
				if (layout.equalsIgnoreCase("create")) {
					fieldPermissionsList = fieldPermissionService.fieldPermissionForCreateLayout(permission);
				} else {
					fieldPermissionsList = fieldPermissionService.fieldPermissionForEditLayout(permission, module,
							dataId);
				}
			}
		}

		return fieldPermissionsList;

	}

}
