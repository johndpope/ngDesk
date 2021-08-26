package com.ngdesk.repositories.approval;

import java.util.Map;
import java.util.Optional;

import com.ngdesk.graphql.approval.dao.Approval;

public interface CustomApprovalRepository {

	public Optional<Approval> findOngoingApproval(String dataId, String companyId, String moduleId, String userId);

	Optional<Approval> findDeniedApproval(String dataId, String companyId, String moduleId, String userId);

	public Optional<Map<String, Object>> findUserById(String userId, String collectionName);

}
