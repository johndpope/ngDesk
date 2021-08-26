package com.ngdesk.repositories.approval;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import com.ngdesk.workflow.approval.dao.Approval;

public class CustomApprovalRepositoryImpl implements CustomApprovalRepository {

	@Autowired
	MongoOperations mongoOperations;

	@Override
	public Optional<Approval> findOngoingApproval(String dataId,String nodeId, String workflowId, String companyId, String moduleId) {
		Criteria criteria = new Criteria();

		criteria.andOperator(Criteria.where("dataId").is(dataId), Criteria.where("moduleId").is(moduleId),
				Criteria.where("companyId").is(companyId),Criteria.where("nodeId").is(nodeId), Criteria.where("workflowId").is(workflowId),
				Criteria.where("status").ne("REJECTED"));
		
		Query query = new Query(criteria);
		return Optional.ofNullable(mongoOperations.findOne(query, Approval.class, "approval"));
	}

}
