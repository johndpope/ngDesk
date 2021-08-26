package com.ngdesk.graphql.modules.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.repositories.modules.ModulesRepository;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

@Component
public class FieldsDataFetcher implements DataFetcher<List<ModuleField>> {

	@Autowired
	AuthManager authManager;

	@Autowired
	ModulesRepository modulesRepository;

	@Override
	public List<ModuleField> get(DataFetchingEnvironment environment) throws Exception {

		String companyId = authManager.getUserDetails().getCompanyId();
		List<Module> modules = modulesRepository.findAllModules("modules_" + companyId);

		ObjectMapper mapper = new ObjectMapper();
		if (environment.getSource() != null) {
			try {
				Map<String, Object> source = mapper.readValue(mapper.writeValueAsString(environment.getSource()),
						Map.class);
				String fieldName = environment.getField().getName();
				List<String> fields = (List<String>) source.get(fieldName);

				Module currentModule = null;
				for (Module module : modules) {

					Optional<ModuleField> optionalField = module.getFields().stream()
							.filter(field -> field.getFieldId().equals(fields.get(0))).findFirst();
					if (optionalField.isPresent()) {
						currentModule = module;
						break;
					}
				}
				if (currentModule != null) {
					List<ModuleField> moduleFields = currentModule.getFields().stream()
							.filter(field -> fields.contains(field.getFieldId())).collect(Collectors.toList());
					return moduleFields;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return null;
	}

}
