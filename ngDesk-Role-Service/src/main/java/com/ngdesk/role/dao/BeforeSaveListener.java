package com.ngdesk.role.dao;

import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertEvent;
import org.springframework.stereotype.Component;

import com.ngdesk.commons.exceptions.BadRequestException;
import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.repositories.ModuleRepository;
import com.ngdesk.repositories.RoleRepository;
import com.ngdesk.role.module.dao.Module;
import com.ngdesk.role.module.dao.ModuleField;

@Component("saveListener")
public class BeforeSaveListener extends AbstractMongoEventListener<Role> {

	@Autowired
	RoleRepository roleRepository;
	@Autowired
	AuthManager authManager;

	@Autowired
	ModuleRepository moduleRepository;

	@Override
	public void onBeforeConvert(BeforeConvertEvent<Role> event) {
		Role role = event.getSource();
		validateDuplicateName(role);
		validateModuleAndField(role);
		validateFieldPermission(role);
	}

	public void validateDuplicateName(Role role) {
		if (role.getId() == null || role.getId() == "") {
			Optional<Role> optionalRole = roleRepository.findRoleByName(role.getName(),
					"roles_" + authManager.getUserDetails().getCompanyId());
			if (optionalRole.isPresent()) {
				String[] variables = { "ROLE", "Name" };
				throw new BadRequestException("DAO_VARIABLE_ALREADY_EXISTS", variables);
			}
		} else {
			Optional<Role> optionalRole = roleRepository.findRoleByNameAndRoleId(role.getName(), role.getId(),
					"roles_" + authManager.getUserDetails().getCompanyId());
			if (optionalRole.isPresent()) {
				String[] variables = { "ROLE", "Name" };
				throw new BadRequestException("DAO_VARIABLE_ALREADY_EXISTS", variables);
			}
		}
	}

	public void validateModuleAndField(Role role) {
		List<Permission> permissions = role.getPermissions();
		for (Permission permission : permissions) {
			if (!(permission.getModule().equalsIgnoreCase("Escalations")
					|| permission.getModule().equalsIgnoreCase("Schedules"))) {
				Optional<Module> optionalPermission = moduleRepository.findById(permission.getModule(),
						"modules_" + authManager.getUserDetails().getCompanyId());
				if (optionalPermission.isEmpty()) {

					throw new BadRequestException("MODULE_INVALID", null);
				}
				List<ModuleField> fields = optionalPermission.get().getFields();
				List<FieldPermission> fieldPermissions = permission.getFieldPermissions();
				for (FieldPermission fieldPermission : fieldPermissions) {
					String roleFields = fieldPermission.getFieldId();
					Optional<ModuleField> fieldId = fields.stream()
							.filter(moduleField -> moduleField.getFieldId().equals(roleFields)).findAny();
					if (!fieldId.isPresent()) {
						String[] var = { optionalPermission.get().getName() };
						throw new BadRequestException("INVALID_FIELD", var);
					}
				}
			}
		}
	}

	public void validateFieldPermission(Role role) {
		List<Permission> permissions = role.getPermissions();
		for (Permission permission : permissions) {
			List<FieldPermission> fieldPermissions = permission.getFieldPermissions();
			for (FieldPermission fieldPermission : fieldPermissions) {
				Pattern intervalTpyePattern = Pattern
						.compile("Read/Write|Not Set|Read|Write Only Creator|Not Editable|Write by team");
				Matcher intervalMatcher = intervalTpyePattern.matcher(fieldPermission.getPermission());
				if (!intervalMatcher.find()) {
					String[] var = { permission.getModule() };
					throw new BadRequestException("INVALID_PERMISSION", var);
				}
			}
		}
	}
}