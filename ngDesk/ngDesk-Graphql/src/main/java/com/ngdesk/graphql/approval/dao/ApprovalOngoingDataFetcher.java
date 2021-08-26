package com.ngdesk.graphql.approval.dao;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ngdesk.commons.managers.AuthManager;
import com.ngdesk.repositories.approval.ApprovalRepository;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

@Component
public class ApprovalOngoingDataFetcher implements DataFetcher<Approval> {

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
		
		Optional<Map<String, Object>> optionalUser = approvalRepository.findUserById(userId, "Users_" + companyId);
		if (optionalUser.isEmpty()) {
			return null;
		}
		Map<String, Object> user = optionalUser.get();
		List<String> teams = (List<String>) user.get("TEAMS");

		Optional<Approval> optionalApproval = approvalRepository.findOngoingApproval(dataId, companyId, moduleId,
				userId);
		if (optionalApproval.isPresent()) {
			boolean isTeamPresent = false;
			Approval result = optionalApproval.get();
			if (result.getApprovedBy() != null && result.getApprovedBy().contains(userId)) {
				result.setDisplayButton(false);
				return result;
			} else if (result.getDeniedBy() != null && result.getDeniedBy().size() > 0) {
				for (DeniedBy deniedBy : result.getDeniedBy()) {
					if (deniedBy.getDeniedUser().equals(userId)) {
						result.setDisplayButton(false);
						break;
					}
				}
			} else {
				if (result.getApprovers().contains(userId)) {
					result.setDisplayButton(true);
					return result;
				}
				for (String teamId : teams) {
					if (result.getTeamsWhoCanApprove().contains(teamId)) {
						isTeamPresent = true;
						break;
					}
				}
				if (isTeamPresent) {
					result.setDisplayButton(true);
				} else {
					result.setDisplayButton(false);
				}
				return result;
			}
		}

		return null;
	}

}
