package com.ngdesk.graphql.modules.data.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

@Component
public class PicklistMultiselectDataFetcher implements DataFetcher<List<String>> {

	@Override
	public List<String> get(DataFetchingEnvironment environment) {

		List<String> picklistValues = new ArrayList<String>();
		Map<String, Object> entry = environment.getSource();
		String fieldName = environment.getField().getName();
		try {
			picklistValues = (List<String>) entry.get(fieldName);
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (picklistValues.size() > 0) {
			return picklistValues;
		}
		return null;
	}
}
