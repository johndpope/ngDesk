package com.ngdesk.graphql.modules.data.dao;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ngdesk.commons.exceptions.ForbiddenException;
import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.commons.managers.SessionManager;
import com.ngdesk.graphql.CustomGraphqlException;
import com.ngdesk.graphql.modules.dao.Module;
import com.ngdesk.graphql.role.layout.dao.RoleService;
import com.ngdesk.repositories.modules.ModulesRepository;
import com.ngdesk.repositories.modules.data.ModuleEntryRepository;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLObjectType;

@Component
public class IndividualEntryFetcher implements DataFetcher<Map<String, Object>> {

	@Autowired
	ModuleEntryRepository entryRepository;

	@Autowired
	ModulesRepository modulesRepository;

	@Autowired
	AuthManager authManager;

	@Autowired
	SessionManager sessionManager;

	@Autowired
	RoleService roleService;

	@Autowired
	DataService dataService;

	ObjectMapper mapper = new ObjectMapper();

	@Override
	public Map<String, Object> get(DataFetchingEnvironment environment) {
		try {
			GraphQLObjectType type = (GraphQLObjectType) environment.getFieldType();
			String companyId = authManager.getUserDetails().getCompanyId();
			Map<String, Object> entry;

			entry = mapper.readValue(mapper.writeValueAsString(environment.getSource()), Map.class);

			List<Module> modules = modulesRepository.findAllModules("modules_" + companyId);

			sessionManager.getSessionInfo().put("modulesMap", modules);
			Module currentModule = modules.stream()
					.filter(module -> module.getName().replaceAll("\\s+", "_").equals(type.getName())).findFirst()
					.get();
			sessionManager.getSessionInfo().put("currentModule", currentModule);

			if (!roleService.isAuthorizedForRecord(authManager.getUserDetails().getRole(), "GET",
					currentModule.getModuleId())) {
				throw new ForbiddenException("FORBIDDEN");
			}

			String entryId = null;
			if (entry == null) {
				entryId = environment.getArgument("id");
			} else {
				String fieldName = environment.getField().getName();
				entryId = entry.get(fieldName).toString();
			}
			Optional<Map<String, Object>> optionalEntry = entryRepository.findEntryById(entryId,
					type.getName().replaceAll("\\s+", "_") + "_" + companyId);

			if (optionalEntry.isPresent()) {
				if (!roleService.isSystemAdmin(authManager.getUserDetails().getRole())) {
					Set<String> teamIds = dataService.getAllTeamIds(false);
					if (optionalEntry.get().get("TEAMS") != null) {
						List<String> teamsOnEntry = (List<String>) optionalEntry.get().get("TEAMS");
						if (!dataService.isAutorizedForRecord(teamIds, teamsOnEntry)) {
							throw new CustomGraphqlException(400, "FORBIDDEN", null);
						}
					}
				}

				return optionalEntry.get();
			} else {
				throw new CustomGraphqlException(400, "INVALID_ENTRY", null);
			}
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return null;
	}

}
