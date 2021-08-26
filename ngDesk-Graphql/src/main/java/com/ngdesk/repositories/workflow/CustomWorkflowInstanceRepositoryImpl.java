package com.ngdesk.repositories.workflow;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import com.ngdesk.graphql.workflow.WorkflowInstance;

public class CustomWorkflowInstanceRepositoryImpl implements CustomWorkflowInstanceRepository {

	@Autowired
	MongoOperations mongoOperations;

	@Override
	public WorkflowInstance getInExecutionInstance(String dataId, List<String> entryIds, String collectionName) {
		Criteria criteria = new Criteria();
		Query query = new Query(criteria.andOperator(Criteria.where("DATA_ID").is(dataId),
				Criteria.where("WORKFLOW_ID").in(entryIds), Criteria.where("STATUS").is("IN_EXECUTION")));

		query.with(Sort.by(Sort.Direction.DESC, "DATE_UPDATED"));
		return mongoOperations.findOne(query, WorkflowInstance.class, collectionName);
	}

	@Override
	public WorkflowInstance getCompletedInstance(String dataId, List<String> entryIds, String collectionName) {
		Criteria criteria = new Criteria();
		Query query = new Query(criteria.andOperator(Criteria.where("DATA_ID").is(dataId),
				Criteria.where("WORKFLOW_ID").in(entryIds), Criteria.where("STATUS").is("COMPLETED")));

		query.with(Sort.by(Sort.Direction.DESC, "DATE_UPDATED"));
		return mongoOperations.findOne(query, WorkflowInstance.class, collectionName);
	}

	@Override
	public WorkflowInstance findByWorkflowIdAndDataId(String workflowId, String dataId, String collectionName) {
		Criteria crietria = new Criteria();
		Query query = new Query(crietria.andOperator(Criteria.where("WORKFLOW_ID").is(workflowId),
				Criteria.where("DATA_ID").is(dataId)));
		return mongoOperations.findOne(query, WorkflowInstance.class, collectionName);
	}

	@Override
	public WorkflowInstance findByWorkflowInstanceByModuleId(String moduleId, String dataId, String workflowId,
			String collectionName) {

		Criteria crietria = new Criteria();
		Query query = new Query(crietria.andOperator(Criteria.where("MODULE_ID").is(moduleId),
				Criteria.where("DATA_ID").is(dataId), Criteria.where("WORKFLOW_ID").is(workflowId)));
		query.with(Sort.by(Sort.Direction.DESC, "DATE_UPDATED"));
		return mongoOperations.findOne(query, WorkflowInstance.class, collectionName);
	}

}
