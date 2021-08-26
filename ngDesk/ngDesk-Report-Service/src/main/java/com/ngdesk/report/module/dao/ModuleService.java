package com.ngdesk.report.module.dao;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;

@Component
public class ModuleService {

	public List<Module> getModuleFamily(Module currentModule, List<Module> modules) {
		List<Module> moduleFamily = new ArrayList<Module>();
		moduleFamily.add(currentModule);

		while (currentModule != null && currentModule.getParentModule() != null) {
			String parentModuleId = currentModule.getParentModule();
			currentModule = modules.stream().filter(module -> module.getModuleId().equals(parentModuleId)).findFirst()
					.orElse(null);
			if (currentModule != null) {
				moduleFamily.add(currentModule);
			} else {
				break;
			}
		}
		return moduleFamily;
	}

	public List<ModuleField> getAllFields(Module currentModule, List<Module> modules) {
		List<ModuleField> allFields = new ArrayList<ModuleField>();
		List<Module> moduleFamily = getModuleFamily(currentModule, modules);
		Set<String> fieldNames = new HashSet<String>();
		moduleFamily.forEach(module -> {
			module.getFields().forEach(field -> {
				if (!fieldNames.contains(field.getName())) {
					allFields.add(field);
					fieldNames.add(field.getName());
				}
			});
		});
		return allFields;
	}

	public String getCollectionName(String moduleName, String companyId) {
		return moduleName.replaceAll("\\s+", "_") + "_" + companyId;
	}

}
