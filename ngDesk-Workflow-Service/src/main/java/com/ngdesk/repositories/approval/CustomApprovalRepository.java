package com.ngdesk.repositories.approval;

import java.util.Optional;

import com.ngdesk.workflow.approval.dao.Approval;

public interface CustomApprovalRepository {

	public Optional<Approval> findOngoingApproval(String dataId,String nodeId, String workflowId, String companyId, String moduleId);
}
