package com.ngdesk.graphql.modules.data.dao;

import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.repositories.modules.ModulesRepository;
import com.ngdesk.repositories.modules.data.ModuleEntryRepository;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLObjectType;

@Component
public class RelationshipEntryDataFetcher implements DataFetcher<Map<String, Object>> {
	@Autowired
	ModuleEntryRepository entryRepository;

	@Autowired
	ModulesRepository modulesRepository;

	@Autowired
	AuthManager authManager;

	ObjectMapper mapper = new ObjectMapper();

	@Override
	public Map<String, Object> get(DataFetchingEnvironment environment) throws Exception {
		try {
			GraphQLObjectType type = (GraphQLObjectType) environment.getFieldType();
			String companyId = authManager.getUserDetails().getCompanyId();
			Map<String, Object> entry = mapper.readValue(mapper.writeValueAsString(environment.getSource()), Map.class);

			String entryId = null;
			if (entry == null) {
				entryId = environment.getArgument("id");
			} else {
				String fieldName = environment.getField().getName();
				if (entry.get(fieldName) != null) {
					entryId = entry.get(fieldName).toString();
				}
			}
			if (entryId != null) {
				Optional<Map<String, Object>> optionalEntry = entryRepository.findEntryById(entryId,
						type.getName().replaceAll("\\s+", "_") + "_" + companyId);
				if (optionalEntry.isPresent()) {
					return optionalEntry.get();
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

}
