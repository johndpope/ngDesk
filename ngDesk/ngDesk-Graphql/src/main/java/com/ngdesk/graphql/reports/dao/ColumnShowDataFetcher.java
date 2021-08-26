package com.ngdesk.graphql.reports.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
public class ColumnShowDataFetcher implements DataFetcher<List<ModuleField>> {

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
				String moduleId = (String) source.get("module");
				List<String> fields = (List<String>) source.get(fieldName);
				Module currentModule = null;
				for (Module module : modules) {
					if (module.getModuleId().equals(moduleId)) {
						currentModule = module;
						break;
					}
				}
				if (currentModule != null) {
					List<ModuleField> allModuleFields = currentModule.getFields();
					List<ModuleField> resultModuleFields = new ArrayList<ModuleField>();
					for (String fieldId : fields) {
						for (ModuleField moduleField : allModuleFields) {
							if (moduleField.getFieldId().equals(fieldId)) {
								resultModuleFields.add(moduleField);
								break;
							}
						}
					}
					return resultModuleFields;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return null;
	}

}
