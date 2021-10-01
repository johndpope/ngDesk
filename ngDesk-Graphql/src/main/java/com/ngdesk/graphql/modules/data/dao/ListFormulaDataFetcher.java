package com.ngdesk.graphql.modules.data.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.commons.managers.SessionManager;
import com.ngdesk.repositories.modules.ModulesRepository;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

@Component
public class ListFormulaDataFetcher implements DataFetcher<List<Map<String, Object>>> {

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
	public List<Map<String, Object>> get(DataFetchingEnvironment environment) throws Exception {

		Map<String, Object> source = mapper.readValue(mapper.writeValueAsString(environment.getSource()), Map.class);

		String fieldName = environment.getField().getName();
		if (source.get(fieldName) == null) {
			return null;
		}
		List<ListFormulaFieldValue> listFormulaFieldValues = mapper.readValue(
				mapper.writeValueAsString(source.get(fieldName)),
				mapper.getTypeFactory().constructCollectionType(List.class, ListFormulaFieldValue.class));
		List<ListFormulaFieldValue> finalListFormulaFieldValues = new ArrayList<ListFormulaFieldValue>();

		for (ListFormulaFieldValue listFormulaFieldValue : listFormulaFieldValues) {
			Object value = listFormulaFieldValue.getValue();
			if (NumberUtils.isParsable(value.toString())) {
				Float floatValue = Float.valueOf(value.toString());
				if (floatValue % 1 != 0) {
					value = (float) (Math.round(floatValue * 100.0) / 100.0);
				} else {
					value = floatValue.intValue();
				}
			}
			listFormulaFieldValue.setValue(value);
			finalListFormulaFieldValues.add(listFormulaFieldValue);
		}
		List<Map<String, Object>> finalListFormulaFieldValue = mapper.readValue(
				mapper.writeValueAsString(finalListFormulaFieldValues),
				mapper.getTypeFactory().constructCollectionType(List.class, Map.class));

		return finalListFormulaFieldValue;

	}

}
