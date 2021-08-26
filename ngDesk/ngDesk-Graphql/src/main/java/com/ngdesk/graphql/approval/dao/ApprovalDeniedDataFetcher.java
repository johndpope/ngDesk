package com.ngdesk.graphql.approval.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.repositories.approval.ApprovalRepository;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

@Component
public class ApprovalDeniedDataFetcher implements DataFetcher<Approval> {

	@Autowired
	AuthManager authManager;

	@Autowired
	ApprovalRepository approvalRepository;

	@Override
	public Approval get(DataFetchingEnvironment environment) throws Exception {
		String companyId = authManager.getUserDetails().getCompanyId();
		String dataId = environment.getArgument("id");
		String moduleId = environment.getArgument("moduleId");
		String userId = authManager.getUserDetails().getUserId();
		List<String> teams = (List<String>) authManager.getUserDetails().getAttributes().get("TEAMS");

		Optional<Approval> optionalApproval = approvalRepository.findDeniedApproval(dataId, companyId, moduleId,
				userId);
		if (optionalApproval.isPresent()) {
			Approval result = optionalApproval.get();
			result.setDisplayButton(false);
			return result;
		}
		return null;
	}
}
