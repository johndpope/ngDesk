package com.ngdesk.graphql.escalation.dao;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ngdesk.repositories.escalation.EscalationRepository;
import com.ngdesk.commons.managers.AuthManager;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

@Component
public class EscalationDataFetcher implements DataFetcher<Escalation> {

	@Autowired
	EscalationRepository escalationRepository;

	@Autowired
	AuthManager authManager;

	@Override
	public Escalation get(DataFetchingEnvironment environment) throws Exception {

		String companyId = authManager.getUserDetails().getCompanyId();
		String escalationId = environment.getArgument("escalationId");
		Optional<Escalation> optionalEscalation = escalationRepository.findById(escalationId, "escalations_"+companyId);
		if (optionalEscalation != null) {
			return optionalEscalation.get();
		}
		return null;

	}

}
