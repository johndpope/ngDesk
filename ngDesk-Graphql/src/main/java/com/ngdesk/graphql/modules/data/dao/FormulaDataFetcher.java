package com.ngdesk.graphql.modules.data.dao;

import java.text.DecimalFormat;
import java.util.Map;

import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.commons.managers.SessionManager;
import com.ngdesk.graphql.modules.dao.Module;
import com.ngdesk.repositories.modules.ModulesRepository;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

@Component
public class FormulaDataFetcher implements DataFetcher<Object> {

	@Autowired
	ModulesRepository modulesRepository;

	@Autowired
	AuthManager authManager;

	@Autowired
	DataService dataService;

	@Autowired
	SessionManager sessionManager;

	ObjectMapper mapper = new ObjectMapper();

	@Override
	public Object get(DataFetchingEnvironment environment) throws Exception {

		Map<String, Object> source = mapper.readValue(mapper.writeValueAsString(environment.getSource()), Map.class);

		String fieldName = environment.getField().getName();
		Object value = source.get(fieldName);
		Module currentModule = (Module) sessionManager.getSessionInfo().get("currentModule");

		if (NumberUtils.isParsable(value.toString())) {
			Float floatValue = Float.valueOf(value.toString());
			if (floatValue % 1 != 0) {
				value = (float) (Math.round(floatValue * 100.0) / 100.0);
			} else {
				value = floatValue.intValue();
			}
		}
		return value;
	}
}
