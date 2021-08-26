package com.ngdesk.graphql.slas.dao;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.repositories.sla.SLARepository;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

@Component
public class SLADataFetcher implements DataFetcher<SLA> {

	@Autowired
	AuthManager authManager;

	@Autowired
	SLARepository slaRepository;

	@Override
	public SLA get(DataFetchingEnvironment environment) throws Exception {
		String companyId = authManager.getUserDetails().getCompanyId();
		String moduleId = environment.getArgument("moduleId");
		String slaId = environment.getArgument("slaId");
		Optional<SLA> optionalSla = slaRepository.findSlaById(companyId, moduleId, slaId, "slas");
		if (optionalSla.isPresent()) {
			return optionalSla.get();
		}
		return null;
	}

}