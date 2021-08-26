package com.ngdesk.module.layouts.mobile.dao;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ngdesk.commons.exceptions.BadRequestException;
import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.module.dao.Module;
import com.ngdesk.module.field.dao.ModuleField;
import com.ngdesk.module.layout.dao.CreateEditLayout;
import com.ngdesk.module.mobile.layout.dao.CreateEditMobileLayout;
import com.ngdesk.module.role.dao.Role;
import com.ngdesk.repositories.FieldRepository;
import com.ngdesk.repositories.ModuleRepository;
import com.ngdesk.repositories.RoleRepository;

@Component
public class EditMobileLayoutService {

	@Autowired
	ModuleRepository moduleRepository;

	@Autowired
	AuthManager authManager;

	@Autowired
	RoleRepository roleRepository;

	public void isValidModuleId(String moduleId, String collectionName) {
		Optional<Module> optionalModule;
		optionalModule = moduleRepository.findById(moduleId, "modules_" + authManager.getUserDetails().getCompanyId());
		if (optionalModule.isEmpty()) {

			throw new BadRequestException("MODULE_ID_INVALID", null);
		}
	}

	public void isValidRoleId(String id, String collectionName) {
		Optional<Role> optionalRole;

		optionalRole = roleRepository.findById(id, "roles_" + authManager.getUserDetails().getCompanyId());
		if (optionalRole.isEmpty()) {

			throw new BadRequestException("ROLE_ID_INVALID", null);
		}

	}

	public void isValidFieldId(List<String> fieldId, String moduleId, String collectionName) {

		Optional<Module> optionalModule = moduleRepository.findById(moduleId, collectionName);
		Module module = optionalModule.get();
		List<String> fieldModuleId = new ArrayList<String>();
		for (int i = 0; i < module.getFields().size(); i++) {
			fieldModuleId.add(module.getFields().get(i).getFieldId());
		}
		if (optionalModule.isEmpty() == false) {

			for (int j = 0; j < fieldId.size(); j++) {
				{
					if (!fieldModuleId.contains(fieldId.get(j))) {
						throw new BadRequestException("FIELD_ID_INVALID", null);
					}
				}
			}
		}
	}

	public void isLayoutValid(String moduleId, String layoutId, String collectionName) {
		Optional<Module> optionalModule = moduleRepository.findById(moduleId, collectionName);
		List<CreateEditMobileLayout> editLayouts = optionalModule.get().getEditMobileLayout();
		List<String> layoutIds = new ArrayList<String>();
		for (int i = 0; i < editLayouts.size(); i++) {
			layoutIds.add(editLayouts.get(i).getLayoutId());
		}

		if (!(layoutIds.contains(layoutId))) {
			throw new BadRequestException("INVALID_LAYOUT_ID", null);
		}
	}

	public void checkForDuplicateRoleId(String roleId, String moduleId, String collectionName, String layoutId) {
		Optional<Module> optionalModule = moduleRepository.findById(moduleId, collectionName);
		Module module = optionalModule.get();

		List<String> roleModId = new ArrayList<String>();
		for (int i = 0; i < module.getEditMobileLayout().size(); i++) {
			if (!module.getEditMobileLayout().get(i).getLayoutId().equals(layoutId)) {
				roleModId.add(module.getEditMobileLayout().get(i).getRole());
			}

		}
		if (roleModId.contains(roleId)) {
			throw new BadRequestException("DUPLICATE_ROLE_ID", null);
		}

	}

}
