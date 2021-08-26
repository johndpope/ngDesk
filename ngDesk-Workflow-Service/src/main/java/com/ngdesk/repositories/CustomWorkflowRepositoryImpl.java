package com.ngdesk.repositories;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import com.ngdesk.workflow.dao.Workflow;

@Repository
public class CustomWorkflowRepositoryImpl implements CustomWorkflowRepository {

	@Autowired
	private MongoOperations mongoOperations;

	@Override
	public Optional<Workflow> findWorkflowByName(String name, String collectionName) {
		return Optional.ofNullable(
				mongoOperations.findOne(new Query(Criteria.where("NAME").is(name)), Workflow.class, collectionName));
	}

	@Override
	public Optional<Workflow> findOtherWorkflowWithDuplicateName(String name, String moduleId, String workflowId,
			String companyId, String collectionName) {
		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("_id").ne(workflowId), Criteria.where("COMPANY_ID").is(companyId),
				Criteria.where("MODULE_ID").is(moduleId), Criteria.where("NAME").is(name));

		Query query = new Query(criteria);

		return Optional.ofNullable(mongoOperations.findOne(query, Workflow.class, collectionName));
	}

	public Page<Workflow> findAllWorkflows(Pageable pageable, String moduleId, String companyId,
			String collectionName) {
		Criteria criteria = new Criteria();
		long count = count(collectionName, companyId, moduleId);
		criteria.andOperator(Criteria.where("MODULE_ID").is(moduleId), Criteria.where("COMPANY_ID").is(companyId));
		List<Workflow> list = findAll(new Query(criteria).with(pageable), collectionName);
		return new PageImpl<>(list, pageable, count);
	}

	public List<Workflow> findAllWorkflows(String moduleId, String companyId, String collectionName) {
		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("MODULE_ID").is(moduleId), Criteria.where("COMPANY_ID").is(companyId));
		List<Workflow> list = new ArrayList<Workflow>();
		list = findAll(new Query(criteria), collectionName);
		return list;
	}

	public long count(String collectionName, String companyId, String moduleId) {
		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("MODULE_ID").is(moduleId), Criteria.where("COMPANY_ID").is(companyId));
		return mongoOperations.count(new Query(criteria), collectionName);
	}

	private List<Workflow> findAll(@Nullable Query query, String collectionName) {
		if (query == null) {
			return Collections.emptyList();
		}
		return mongoOperations.find(query, Workflow.class, collectionName);
	}

	@Override
	public Optional<Workflow> findWorkflowById(String workflowId, String moduleId, String companyId,
			String collectionName) {
		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("_id").is(workflowId), Criteria.where("MODULE_ID").is(moduleId),
				Criteria.where("COMPANY_ID").is(companyId));
		return Optional.ofNullable(mongoOperations.findOne(new Query(criteria), Workflow.class, collectionName));
	}

	@Override
	public void deleteWorkflow(String moduleId, String companyId, String workflowId, String collectionName) {
		Assert.notNull(moduleId, "The given id must not be null!");
		Assert.notNull(workflowId, "The given id must not be null!");
		mongoOperations.remove(getIdQuery(workflowId, moduleId, companyId), Workflow.class, collectionName);
	}

	private Query getIdQuery(Object workflowId, String moduleId, String companyId) {
		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("MODULE_ID").is(moduleId), Criteria.where("COMPANY_ID").is(companyId),
				Criteria.where("_id").is(workflowId));
		return new Query(criteria);
	}

	@Override
	public Optional<Workflow> findWorkFlowOrder(String workflowId, int order, String moduleId, String companyId,
			String collectionName) {
		Criteria criteria = new Criteria();
		criteria.andOperator(Criteria.where("_id").ne(workflowId), Criteria.where("COMPANY_ID").is(companyId),
				Criteria.where("MODULE_ID").is(moduleId), Criteria.where("ORDER").is(order));

		Query query = new Query(criteria);

		return Optional.ofNullable(mongoOperations.findOne(query, Workflow.class, collectionName));
	}

}
