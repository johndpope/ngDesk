package com.ngdesk.data.modules.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ngdesk.commons.exceptions.BadRequestException;
import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.repositories.module.entry.ModulesRepository;

@Component
public class ModuleService {

	@Autowired
	ModulesRepository modulesRepository;

	public List<Module> getModuleFamily(String moduleId, String companyId) {
		List<Module> modules = new ArrayList<Module>();
		while (moduleId != null) {
			Optional<Module> optionalModule = modulesRepository.findById(moduleId,
					"modules_" + companyId);
			if (optionalModule.isPresent()) {
				modules.add(0, optionalModule.get());
				moduleId = optionalModule.get().getParentModule();
			} else {
				break;
			}
		}
		return modules;
	}

	public String getCollectionName(String moduleName, String companyId) {
		return moduleName.replaceAll("\\s+", "_") + "_" + companyId;
	}

	public List<ModuleField> getAllFields(Module currentModule, String companyId) {
		List<ModuleField> allFields = new ArrayList<ModuleField>();

		allFields.addAll(currentModule.getFields());

		List<Module> moduleFamily = getModuleFamily(currentModule.getModuleId(), companyId);

		List<Module> filteredModules = moduleFamily.stream()
				.filter(module -> !module.getModuleId().equals(currentModule.getModuleId()))
				.collect(Collectors.toList());

		for (int i = filteredModules.size() - 1; i >= 0; i--) {
			Module module = filteredModules.get(i);
			for (ModuleField field : module.getFields()) {
				field.setInheritedField(true);
				field.setInheritanceLevel("output" + i);
				allFields.add(field);
			}
		}

		return allFields;
	}

	public Module getRelationshipModule(ModuleField field, String companyId) {
		Optional<Module> optionalRelationshipModule = modulesRepository.findById(field.getModule(),
				"modules_" + companyId);

		if (optionalRelationshipModule.isEmpty()) {
			String[] vars = { field.getName() };
			throw new BadRequestException("RELATIONSHIP_MODULE_INVALID", vars);
		}
		return optionalRelationshipModule.get();
	}
	
}
