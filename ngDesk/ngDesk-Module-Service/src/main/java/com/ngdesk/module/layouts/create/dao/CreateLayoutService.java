package com.ngdesk.module.layouts.create.dao;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ngdesk.commons.exceptions.BadRequestException;
import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.module.dao.Module;
import com.ngdesk.module.layout.dao.CreateEditLayout;
import com.ngdesk.repositories.ModuleRepository;
import com.ngdesk.repositories.RoleRepository;

@Component
public class CreateLayoutService {

	@Autowired
	ModuleRepository moduleRepository;

	@Autowired
	RoleRepository roleRepoistory;

	@Autowired
	AuthManager authManager;

	public void checkDuplicateLayoutForRole(Module module, String currentRole) {
		Optional<CreateEditLayout> optionalCreateLayout = module.getCreateLayout().stream()
				.filter(createLayout -> createLayout.getRole().equals(currentRole)).findFirst();

		if (optionalCreateLayout.isPresent()) {
			String[] vars = { "CREATE_LAYOUT", "ROLE" };
			throw new BadRequestException("DAO_VARIABLE_ALREADY_EXISTS", vars);
		}
	}

	public CreateEditLayout validateAndGetExistingCreateLayout(String layoutId, Module module) {
		Optional<CreateEditLayout> optionalCreateLayout = module.getCreateLayout().stream()
				.filter(moduleCreateLayout -> moduleCreateLayout.getLayoutId().equalsIgnoreCase(layoutId)).findFirst();

		if (optionalCreateLayout.isEmpty()) {
			String[] vars = { "CREATE_LAYOUT" };
			throw new BadRequestException("DAO_NOT_FOUND", vars);
		}
		
		return optionalCreateLayout.get();

	}

	public void duplicateCheckForPutCall(CreateEditLayout createLayout, Module module) {
		Optional<CreateEditLayout> optionalCreateLayout = module.getCreateLayout().stream().filter(
				moduleCreateLayout -> !moduleCreateLayout.getLayoutId().equalsIgnoreCase(createLayout.getLayoutId())
						&& moduleCreateLayout.getRole().equalsIgnoreCase(createLayout.getRole()))
				.findFirst();
		if (optionalCreateLayout.isPresent()) {
			String[] vars = { "CREATE_LAYOUT", "ROLE" };
			throw new BadRequestException("DAO_VARIABLE_ALREADY_EXISTS", vars);
		}
	}

}