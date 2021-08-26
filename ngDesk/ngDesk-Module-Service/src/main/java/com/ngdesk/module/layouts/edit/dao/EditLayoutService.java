package com.ngdesk.module.layouts.edit.dao;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ngdesk.commons.exceptions.BadRequestException;
import com.ngdesk.module.dao.Module;
import com.ngdesk.module.layout.dao.CreateEditLayout;
import com.ngdesk.repositories.ModuleRepository;
import com.ngdesk.repositories.RoleRepository;

@Component
public class EditLayoutService {

	@Autowired
	ModuleRepository moduleRepository;

	@Autowired
	RoleRepository roleRepository;

	public void checkDuplicateLayoutForRole(Module module, String currentRole) {
		Optional<CreateEditLayout> optionalEditLayout = module.getEditLayout().stream()
				.filter(editLayout -> editLayout.getRole().equals(currentRole)).findFirst();

		if (optionalEditLayout.isPresent()) {
			String[] vars = { "EDIT_LAYOUT", "ROLE" };
			throw new BadRequestException("DAO_VARIABLE_ALREADY_EXISTS", vars);
		}
	}

	public CreateEditLayout validateAndGetExistingEditLayout(String layoutId, Module module) {
		Optional<CreateEditLayout> optionalEditLayout = module.getEditLayout().stream()
				.filter(moduleEditLayout -> moduleEditLayout.getLayoutId().equalsIgnoreCase(layoutId)).findFirst();

		if (optionalEditLayout.isEmpty()) {
			String[] vars = { "EDIT_LAYOUT" };
			throw new BadRequestException("DAO_NOT_FOUND", vars);
		}
		
		return optionalEditLayout.get();

	}

	public void duplicateCheckForPutCall(CreateEditLayout editLayout, Module module) {
		Optional<CreateEditLayout> optionalEditLayout = module.getCreateLayout().stream()
				.filter(moduleEditLayout -> !moduleEditLayout.getLayoutId().equalsIgnoreCase(editLayout.getLayoutId())
						&& moduleEditLayout.getRole().equalsIgnoreCase(editLayout.getRole()))
				.findFirst();
		if (optionalEditLayout.isPresent()) {
			String[] vars = { "EDIT_LAYOUT", "ROLE" };
			throw new BadRequestException("DAO_VARIABLE_ALREADY_EXISTS", vars);
		}
	}
}
