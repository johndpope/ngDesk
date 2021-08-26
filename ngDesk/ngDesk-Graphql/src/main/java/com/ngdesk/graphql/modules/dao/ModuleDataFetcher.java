package com.ngdesk.graphql.modules.dao;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.repositories.modules.ModulesRepository;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

@Component
public class ModuleDataFetcher implements DataFetcher<Module> {

	@Autowired
	ModulesRepository modulesRepository;

	ObjectMapper mapper = new ObjectMapper();

	@Autowired
	AuthManager authManager;

	@Override
	public Module get(DataFetchingEnvironment environment) throws Exception {
		String companyId = authManager.getUserDetails().getCompanyId();

		String fieldName = environment.getField().getName();
		switch (fieldName) {
		case "parentModule":

			Module module = environment.getSource();
			if (module != null && module.getParentModule() != null && !module.getParentModule().isBlank()) {
				String parentModuleId = module.getParentModule();
				Optional<Module> optionalModule = modulesRepository.findById(parentModuleId, "modules_" + companyId);
				if (optionalModule.isPresent()) {
					return optionalModule.get();
				}
			}
			break;
		case "module":
			ModuleField field = environment.getSource();
			if (field != null && field.getModule() != null && !field.getModule().isBlank()) {
				String moduleId = field.getModule();
				Optional<Module> optionalModule = modulesRepository.findById(moduleId, "modules_" + companyId);
				if (optionalModule.isPresent()) {
					return optionalModule.get();
				}
			}
			break;
		default:
			break;
		}
		return null;
	}

}
