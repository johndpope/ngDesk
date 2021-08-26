package com.ngdesk.repositories;

import java.util.List;
import java.util.Optional;

import org.apache.http.util.Asserts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import com.ngdesk.workflow.dao.Workflow;

public class CustomWorkflowRepositoryImpl implements CustomWorkflowRepository {

	@Autowired
	MongoOperations mongoOperations;

	@Override
	public Optional<List<Workflow>> getAllWorkflowTemplates(String moduleId, String collectionName) {

		Query query = new Query(Criteria.where("MODULE_ID").is(moduleId));
		query.fields().exclude("_id");
		return Optional.ofNullable(mongoOperations.find(query, Workflow.class, collectionName));
	}

	@Override
	public Optional<Workflow> getWorkflowByModule(String workflowId, String moduleId, String companyId,
			String collectionName) {

		Asserts.notNull(workflowId, "Workflow Id must not be null");
		Asserts.notNull(moduleId, "Module id must not be null");
		Asserts.notNull(companyId, "Company id must not be null");
		Asserts.notNull(collectionName, "Collection name must not be null");
		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("MODULE_ID").is(moduleId), Criteria.where("_id").is(workflowId),
				Criteria.where("COMPANY_ID").is(companyId));
		Query query = new Query(criteria);

		return Optional.ofNullable(mongoOperations.findOne(query, Workflow.class, collectionName));
	}

	@Override
	public Optional<List<Workflow>> findAllWithModuleIdAndCompanyId(String moduleId, String companyId,
			String collectionName) {
		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("MODULE_ID").is(moduleId), Criteria.where("COMPANY_ID").is(companyId));
		Query query = new Query(criteria);
		return Optional.ofNullable(mongoOperations.find(query, Workflow.class, collectionName));
	}

	@Override
	public Optional<List<Workflow>> findAllWithModuleIdsAndCompanyId(List<String> moduleIds, String companyId,
			String collectionName) {
		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("MODULE_ID").in(moduleIds), Criteria.where("COMPANY_ID").is(companyId));
		Query query = new Query(criteria);
		return Optional.ofNullable(mongoOperations.find(query, Workflow.class, collectionName));
	}

}
