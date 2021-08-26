package com.ngdesk.graphql.modules.dao;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.commons.managers.SessionManager;
import com.ngdesk.repositories.modules.ModulesRepository;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

@Component
public class FieldDataFetcher implements DataFetcher<ModuleField> {

	@Autowired
	ModulesRepository modulesRepository;

	ObjectMapper mapper = new ObjectMapper();

	@Autowired
	AuthManager authManager;
	
	@Autowired
	SessionManager sessionManager;

	@Override
	public ModuleField get(DataFetchingEnvironment environment) throws Exception {
		String fieldName = environment.getField().getName();
		ObjectMapper mapper = new ObjectMapper();

		if (environment.getSource() != null) {
			Map<String, Object> source = mapper.readValue(mapper.writeValueAsString(environment.getSource()),
					Map.class);
			if (source.containsKey(fieldName) && source.get(fieldName) != null) {
				String fieldId = source.get(fieldName).toString();
				Module currentModule = null;
				
				
				List<Module> modules = (List<Module>) sessionManager.getSessionInfo().get("modulesMap");
				for (Module module : modules) {
					Optional<ModuleField> optionalField = module.getFields().stream()
							.filter(field -> field.getFieldId().equals(fieldId)).findFirst();
					if (optionalField.isPresent()) {
						currentModule = module;
						break;
					}
				}
				if (currentModule != null) {
					ModuleField moduleField = currentModule.getFields().stream()
							.filter(field -> field.getFieldId().equals(fieldId)).findFirst().orElse(null);
					return moduleField;
				}
			}
		}
		return null;
	}

}
