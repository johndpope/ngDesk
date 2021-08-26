package com.ngdesk.repositories;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import com.ngdesk.workflow.executor.dao.WorkflowInstance;

@Repository
public class CustomWorkflowInstanceRepositoryImpl implements CustomWorkflowInstanceRepository {

	@Autowired
	MongoOperations mongoOperations;

	@Override
	public List<WorkflowInstance> getPausedWorkflows(String companyId, String moduleId, String dataId,
			String collectionName) {

		Assert.notNull(companyId, "Company ID should not be null");
		Assert.notNull(moduleId, "Data ID should not be null");
		Assert.notNull(dataId, "Module ID should not be null");
		Assert.notNull(collectionName, "Collection name should not be null");

		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("COMPANY_ID").is(companyId), Criteria.where("DATA_ID").is(dataId),
				Criteria.where("MODULE_ID").is(moduleId), Criteria.where("STATUS").is("IN_EXECUTION"));

		Query query = new Query(criteria);

		return mongoOperations.find(query, WorkflowInstance.class, collectionName);

	}
}
