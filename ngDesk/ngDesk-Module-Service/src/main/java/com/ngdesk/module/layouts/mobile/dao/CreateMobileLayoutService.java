package com.ngdesk.module.layouts.mobile.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ngdesk.commons.exceptions.BadRequestException;
import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.module.dao.Module;
import com.ngdesk.module.role.dao.Role;
import com.ngdesk.repositories.ModuleRepository;
import com.ngdesk.repositories.RoleRepository;

@Component
public class CreateMobileLayoutService {
	@Autowired
	ModuleRepository moduleRepository;

	@Autowired
	AuthManager authManager;

	@Autowired
	RoleRepository roleRepository;

	public void isValidModuleId(String moduleId, String collectionName) {
		Optional<Module> optionalModule = moduleRepository.findById(moduleId, collectionName);
		if (optionalModule.isEmpty()) {
			throw new BadRequestException("MODULE_ID_INVALID", null);
		}
	}

	public void isValidRoleId(String roleId, String collectionName) {
		Optional<Role> optionalRole = roleRepository.findById(roleId, collectionName);
		if (optionalRole.isEmpty()) {
			throw new BadRequestException("ROLE_ID_INVALID", null);
		}
	}

	public void checkForDuplicateRoleId(String roleId, String moduleId, String collectionName, String layoutId) {
		Optional<Module> optionalModule = moduleRepository.findById(moduleId, collectionName);
		Module module = optionalModule.get();
		List<String> roleModuleId = new ArrayList<String>();
		for (int i = 0; i < module.getCreateMobileLayout().size(); i++) {
			if (!module.getCreateMobileLayout().get(i).getLayoutId().equals(layoutId)) {
				roleModuleId.add(module.getCreateMobileLayout().get(i).getRole());
			}
		}
		if (roleModuleId.contains(roleId)) {
			throw new BadRequestException("DUPLICATE_ROLE_ID", null);
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
			for (int i = 0; i < fieldId.size(); i++) {
				if (!fieldModuleId.contains(fieldId.get(i))) {
					throw new BadRequestException("FIELD_ID_INVALID", null);
				}
			}
		}
	}

	public void isValidLayoutId(String layoutId, String moduleId, String collectionName) {
		Optional<Module> optionalModule = moduleRepository.findById(moduleId, collectionName);
		Module module = optionalModule.get();
		List<String> modLayoutId = new ArrayList<String>();
		for (int i = 0; i < module.getCreateMobileLayout().size(); i++) {
			modLayoutId.add(module.getCreateMobileLayout().get(i).getLayoutId());
		}
		if (optionalModule.isEmpty() == false) {

			if (!modLayoutId.contains(layoutId)) {
				throw new BadRequestException("INVALID_LAYOUT_ID", null);
			}

		}

	}

}
