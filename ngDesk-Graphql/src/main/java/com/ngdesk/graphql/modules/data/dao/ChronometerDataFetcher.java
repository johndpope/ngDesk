package com.ngdesk.graphql.modules.data.dao;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

@Component
public class ChronometerDataFetcher implements DataFetcher<String> {

	@Autowired
	DataService dataService;

	ObjectMapper mapper = new ObjectMapper();

	@Override
	public String get(DataFetchingEnvironment environment) throws Exception {
		Map<String, Object> source = mapper.readValue(mapper.writeValueAsString(environment.getSource()), Map.class);
		String fieldName = environment.getField().getName();
		if (source.containsKey(fieldName) && source.get(fieldName) != null) {
			Integer value = (Integer) source.get(fieldName);
			return dataService.chronometerFormatTransform(value, "");
		}
		return null;
	}

}
