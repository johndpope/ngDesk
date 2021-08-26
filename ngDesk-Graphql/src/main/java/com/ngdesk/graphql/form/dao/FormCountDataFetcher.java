package com.ngdesk.graphql.form.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.repositories.form.FormRepository;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

@Component
public class FormCountDataFetcher implements DataFetcher<Integer> {

	@Autowired
	AuthManager authManager;

	@Autowired
	FormRepository formRepository;

	@Override
	public Integer get(DataFetchingEnvironment environment) {
		String companyId = authManager.getUserDetails().getCompanyId();
		String moduleId = environment.getArgument("moduleId");
		if (moduleId != null) {
			return formRepository.formsCount(companyId, moduleId, "forms");
		}
		return null;
	}

}
