package com.ngdesk.graphql.role.layout.dao;

import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.graphql.modules.dao.Module;
import com.ngdesk.graphql.modules.dao.ModuleField;
import com.ngdesk.repositories.modules.ModulesRepository;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

@Component
public class RoleLayoutModuleDataFetcher implements DataFetcher<Module> {
	@Autowired
	ModulesRepository modulesRepository;

	ObjectMapper mapper = new ObjectMapper();

	@Autowired
	AuthManager authManager;

	@Override
	public Module get(DataFetchingEnvironment environment) throws Exception {
		String companyId = authManager.getUserDetails().getCompanyId();
		if (environment.getSource() != null) {

			String fieldName = environment.getField().getName();
			Map<String, Object> source = mapper.readValue(mapper.writeValueAsString(environment.getSource()),
					Map.class);
			String module = source.get(fieldName).toString();
			Optional<Module> optionalModule = modulesRepository.findById(module, "modules_" + companyId);
			if (optionalModule.isPresent()) {
				return optionalModule.get();
			}
		}

		return null;
	}

}
