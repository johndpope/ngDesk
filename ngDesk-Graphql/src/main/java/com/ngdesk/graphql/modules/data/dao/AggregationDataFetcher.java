package com.ngdesk.graphql.modules.data.dao;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.commons.managers.SessionManager;
import com.ngdesk.graphql.CustomGraphqlException;
import com.ngdesk.graphql.modules.dao.Condition;
import com.ngdesk.graphql.modules.dao.Module;
import com.ngdesk.graphql.modules.dao.ModuleField;
import com.ngdesk.graphql.modules.dao.ModulesService;
import com.ngdesk.graphql.role.layout.dao.RoleService;
import com.ngdesk.repositories.modules.ModulesRepository;
import com.ngdesk.repositories.modules.data.ModuleEntryRepository;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

@Component
public class AggregationDataFetcher implements DataFetcher<Float> {

	@Autowired
	ModuleEntryRepository moduleEntryRepository;

	@Autowired
	ModulesRepository modulesRepository;

	@Autowired
	AuthManager authManager;

	@Autowired
	SessionManager sessionManager;

	@Autowired
	ModulesService modulesService;

	@Autowired
	RoleService roleService;

	@Autowired
	DataService dataService;

	@Override
	public Float get(DataFetchingEnvironment environment) throws Exception {

		Map<String, Object> source = environment.getSource();
		String fieldName = environment.getField().getName();
		String companyId = authManager.getUserDetails().getCompanyId();
		String role = authManager.getUserDetails().getRole();
		String moduleId = environment.getArgument("moduleId");
		Boolean isAdmin = false;

		if (roleService.isSystemAdmin(role)) {
			isAdmin = true;
		}
		List<Module> modules = modulesRepository.findAllModules("modules_" + companyId);

		Set<String> teamIds = dataService.getAllTeamIds(isAdmin);
		Module currentModule = null;

		if (moduleId == null) {
			currentModule = (Module) sessionManager.getSessionInfo().get("currentModule");
		} else {
			Module relatedModule = modulesRepository
					.findById(moduleId, "modules_" + authManager.getUserDetails().getCompanyId()).orElse(null);
			currentModule = relatedModule;
		}

		ModuleField currentField = currentModule.getFields().stream()
				.filter(field -> field.getName().equals(fieldName)).findFirst().orElse(null);
		if (currentField == null) {
			String[] vars = { fieldName };
			throw new CustomGraphqlException(400, "INVALID_FIELD", vars);
		}

		ModuleField aggregationField = currentModule.getFields().stream()
				.filter(field -> field.getFieldId().equals(currentField.getAggregationField())).findFirst()
				.orElse(null);
		if (aggregationField == null) {
			throw new CustomGraphqlException(400, "INVALID_AGGREGATION_FIELD", null);
		}

		Module relatedModule = modulesRepository
				.findById(aggregationField.getModule(), "modules_" + authManager.getUserDetails().getCompanyId())
				.orElse(null);
		if (relatedModule == null) {
			throw new CustomGraphqlException(400, "INVALID_RELATED_MODULE", null);
		}

		ModuleField aggregationRelatedField = relatedModule.getFields().stream()
				.filter(field -> field.getFieldId().equals(currentField.getAggregationRelatedField())).findFirst()
				.orElse(null);

		if (aggregationRelatedField == null) {
			throw new CustomGraphqlException(400, "INVALID_RELATED_AGGREGATION_FIELD", null);
		}

		ModuleField relationshipField = relatedModule.getFields().stream()
				.filter(field -> field.getFieldId().equals(aggregationField.getRelationshipField())).findFirst()
				.orElse(null);

		if (relationshipField == null) {
			throw new CustomGraphqlException(400, "INVALID_RELATED_FIELD", null);
		}

		List<ModuleField> moduleFields = modulesService.getAllFields(relatedModule, modules);

		List<Condition> conditions = currentField.getConditions();

		Optional<Map<String, Object>> aggregationEntry = moduleEntryRepository.findAggregationFieldValue(
				relationshipField.getName(), source.get("_id").toString(), aggregationRelatedField.getName(),
				currentField.getAggregationType(), teamIds, conditions, modules, moduleFields,
				modulesService.getCollectionName(relatedModule.getName(), authManager.getUserDetails().getCompanyId()));

		if (aggregationEntry.isPresent()) {
			if (aggregationEntry.get().get(aggregationRelatedField.getName()) != null) {
				Float value = Float
						.parseFloat(aggregationEntry.get().get(aggregationRelatedField.getName()).toString());
				value = (float) (Math.round(value * 100.0) / 100.0);
				return value;
			}
		}
		return null;
	}

}
