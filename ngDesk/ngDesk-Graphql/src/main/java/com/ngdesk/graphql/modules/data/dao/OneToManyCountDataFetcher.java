package com.ngdesk.graphql.modules.data.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.graphql.CustomGraphqlException;
import com.ngdesk.graphql.modules.dao.Condition;
import com.ngdesk.graphql.modules.dao.Module;
import com.ngdesk.graphql.modules.dao.ModuleField;
import com.ngdesk.graphql.modules.dao.ModulesService;
import com.ngdesk.repositories.modules.ModulesRepository;
import com.ngdesk.repositories.modules.data.ModuleEntryRepository;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

@Component
public class OneToManyCountDataFetcher implements DataFetcher<Integer> {

	@Autowired
	ModuleEntryRepository entryRepository;

	@Autowired
	AuthManager authManager;

	@Autowired
	ModulesRepository modulesRepository;

	@Autowired
	ModulesService modulesService;

	@Autowired
	DataService dataService;

	@Override
	public Integer get(DataFetchingEnvironment environment) {

		String companyId = authManager.getUserDetails().getCompanyId();
		String moduleId = environment.getArgument("moduleId");
		String fieldId = environment.getArgument("fieldId");
		String dataId = environment.getArgument("dataId");
		List<Module> modules = modulesRepository.findAllModules("modules_" + companyId);

		Optional<Module> optionalModule = modules.stream().filter(module -> module.getModuleId().equals(moduleId))
				.findFirst();
		if (optionalModule.isEmpty()) {
			throw new CustomGraphqlException(400, "INVALID_MODULE", null);
		}

		Module module = optionalModule.get();
		List<ModuleField> moduleFields = modulesService.getAllFields(module, modules);
		Optional<ModuleField> optionalField = module.getFields().stream()
				.filter(moduleField -> moduleField.getFieldId().equals(fieldId)).findFirst();
		if (optionalField.isEmpty()) {
			throw new CustomGraphqlException(400, "INVALID_FIELD", null);
		}
		ModuleField moduleField = optionalField.get();
		if (moduleField.getDataType().getDisplay().equalsIgnoreCase("Relationship")
				&& moduleField.getRelationshipType().equalsIgnoreCase("One To Many")) {
			Optional<Module> optionalRelationshipModule = modules.stream()
					.filter(relatedModule -> relatedModule.getModuleId().equals(moduleField.getModule())).findFirst();

			if (optionalRelationshipModule.isEmpty()) {
				throw new CustomGraphqlException(400, "INVALID_MODULE", null);
			}
			Module relationshipModule = optionalRelationshipModule.get();
			Optional<ModuleField> optionalRelationship = relationshipModule.getFields().stream().filter(
					relatedModuleField -> relatedModuleField.getFieldId().equals(moduleField.getRelationshipField()))
					.findFirst();
			if (optionalRelationship.isEmpty()) {
				throw new CustomGraphqlException(400, "INVALID_RELATED_FIELD", null);
			}
			String relationshipName = optionalRelationship.get().getName();
			List<Condition> conditions = new ArrayList<Condition>();
			Condition filter = new Condition();
			filter.setCondition(optionalRelationship.get().getFieldId());
			filter.setConditionValue(dataId);
			filter.setOpearator("EQUALS_TO");
			filter.setRequirementType("All");
			return entryRepository.getOneToManyCountValue(modules, moduleFields, conditions, relationshipName, dataId,
					modulesService.getCollectionName(relationshipModule.getName(), companyId));
		} else {
			throw new CustomGraphqlException(400, "INVALID_RELATED_FIELD", null);
		}
	}
}
