package com.ngdesk.repositories.approval;

import com.ngdesk.graphql.approval.dao.Approval;
import com.ngdesk.repositories.CustomNgdeskRepository;

public interface ApprovalRepository extends CustomNgdeskRepository<Approval, String>, CustomApprovalRepository {

}
