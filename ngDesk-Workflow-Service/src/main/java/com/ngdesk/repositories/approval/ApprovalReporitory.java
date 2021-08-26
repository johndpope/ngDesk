package com.ngdesk.repositories.approval;

import com.ngdesk.repositories.CustomNgdeskRepository;
import com.ngdesk.workflow.approval.dao.Approval;

public interface ApprovalReporitory extends CustomNgdeskRepository<Approval, String>, CustomApprovalRepository {

}
